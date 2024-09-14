package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Message;
import com.blog.mapper.MessageMapper;
import com.blog.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Autowired
    private MessageMapper messageMapper;


    @Override
    public String getLastMessageById(int userId, int sendId) {
        return "";
    }

    @Override
    public String saveMessageById(int id, int toId, String message) {
        return "";
    }

    @Override
    public List<Message> getMessage(int id, int sid) {
        return null;
    }

    @Override
    public List<Message> getAllMessage(int id) {
        return null;
    }
}

