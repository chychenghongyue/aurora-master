package com.blog.consumer;

import com.alibaba.fastjson.JSON;
import com.blog.model.dto.EmailDTO;
import com.blog.util.EmailUtil;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import static com.blog.constant.RabbitMQConstant.EMAIL_EXCHANGE;
import static com.blog.constant.RabbitMQConstant.EMAIL_QUEUE;

@Component
public class CommentNoticeConsumer {

    @Autowired
    private EmailUtil emailUtil;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = EMAIL_QUEUE,
                    durable = "true"),//惰性队列，消息持久化
            exchange = @Exchange(name = EMAIL_EXCHANGE, type = ExchangeTypes.FANOUT)))
    public void process(byte[] data) {
        EmailDTO emailDTO = JSON.parseObject(new String(data), EmailDTO.class);
        emailUtil.sendHtmlMail(emailDTO);
    }

}
