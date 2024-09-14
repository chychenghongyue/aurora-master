package com.blog.service.impl;

import com.blog.constant.SpringContext;
import com.blog.entity.UserAuth;
import com.blog.entity.UserInfo;
import com.blog.entity.WebSocketInfo;
import com.blog.mapper.UserAuthMapper;
import com.blog.mapper.UserInfoMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

@Component
@Slf4j
@ServerEndpoint("/websocket/{articleId}/{userId}")
public class WebSocketServer {
    private static CopyOnWriteArraySet<WebSocketServer> webSocketsSet = new CopyOnWriteArraySet<>();
    private static ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();
    private final Executor taskExecutor = init();
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAuthMapper userAuthMapper;
    ObjectMapper objectMapper = new ObjectMapper();
    private Set<Long> set = new HashSet<>();
    private Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    private Session session;
    private String sessionId;

    public Executor init() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(25);
        executor.initialize();
        return executor;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("articleId") Long articleId, @PathParam("userId") Long userId) {
        sessionId = articleId + "&" + userId;
        try {
            this.session = session;
            if (sessionPool.containsKey(sessionId)) {
                sessionPool.remove(sessionId);
                sessionPool.put(sessionId, session);
            } else {
                sessionPool.put(sessionId, session);
                webSocketsSet.add(this);
                set.add(userId);
                info(articleId.toString(), userId.toString());
            }
        } catch (Exception e) {
        }
    }

    public void info(String articleId, String userId) {
        System.err.println(set);
        List<String> avatar = new ArrayList<>();
        set.forEach(item -> {
            this.userInfoMapper = SpringContext.getBean(UserInfoMapper.class);
            this.userAuthMapper = SpringContext.getBean(UserAuthMapper.class);
            UserAuth userAuth = userAuthMapper.selectById(item);
            System.err.println(userAuth);
            UserInfo userInfo = userInfoMapper.selectById(userAuth.getUserInfoId());
            avatar.add(userInfo.getAvatar());
        });
        System.err.println(set);

        System.err.println("【websocket消息】在线用户头像:" + avatar + "总数为:" + webSocketsSet.size());
        try {
            WebSocketInfo infoMessage = new WebSocketInfo("avatar", articleId, null, objectMapper.writeValueAsString(sessionPool));
            sendTopicMessage(articleId, infoMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose() {
        try {
            webSocketsSet.remove(this);
            sessionPool.remove(sessionId);
            logger.error("【websocket消息】连接断开:" + sessionId + "总数为:" + webSocketsSet.size());
            String articleId = sessionId.substring(0, sessionId.indexOf("&"));
            String userId = sessionId.substring(sessionId.indexOf("&") + 1);
            set.remove(Long.parseLong(userId));
            info(articleId.toString(), userId.toString());
        } catch (Exception e) {
        }
    }

    @OnMessage
    public void onMessage(String message) {
        WebSocketInfo webSocketInfo;
        try {
            webSocketInfo = objectMapper.readValue(message, WebSocketInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        logger.error("【websocket消息】收到客户端消息:" + webSocketInfo);
        String messageType = webSocketInfo.getType();
        if ("ping".equals(messageType)) {
            String sessionId = webSocketInfo.getArticleId() + "&" + webSocketInfo.getUserId();
            logger.error("sessionId" + sessionId);
            sendOneMessage(sessionId, webSocketInfo);
            logger.error("返回心跳" + webSocketInfo);
        }
        String topic = webSocketInfo.getArticleId();
        String userId = webSocketInfo.getUserId();
        if ("updateContent".equals(messageType)) {
            sendTopicMessage(topic, webSocketInfo);
            logger.error("【websocket消息】收到客户端消息:" + webSocketInfo);
        }
    }

    public void sendTopicMessage(String topic, WebSocketInfo webSocketInfo) {
        for (Map.Entry<String, Session> entry : sessionPool.entrySet()) {
            Session session = entry.getValue();
            if (session != null && session.isOpen()) {
                try {
                    String key = entry.getKey();
                    if (key.startsWith(topic)) { // 检查键是否以给定主题开头
                        logger.error("【websocket消息】 单点消息:" + webSocketInfo);
                        String message = objectMapper.writeValueAsString(webSocketInfo);
                        taskExecutor.execute(() -> {
                            session.getAsyncRemote().sendText(message);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendAllMessage(String message) {
        logger.error("【websocket消息】广播消息:" + message);
        for (WebSocketServer webSocket : webSocketsSet) {
            try {
                if (webSocket.session.isOpen()) {
                    webSocket.session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendOneMessage(String sessionId, WebSocketInfo webSocketInfo) {
        Session session = sessionPool.get(sessionId);
        if (session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(webSocketInfo);
                taskExecutor.execute(() -> {
                    session.getAsyncRemote().sendText(message);
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


        }
    }

    public void sendMoreMessage(String[] sessionIds, String message) {
        for (String sessionId : sessionIds) {
            Session session = sessionPool.get(sessionId);
            if (session != null && session.isOpen()) {
                try {
                    logger.error("【websocket消息】 单点消息:" + message);
                    session.getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("用户错误,原因:" + error.getMessage());
        error.printStackTrace();
    }
}
