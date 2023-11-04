package com.aurora.consumer;

import com.alibaba.fastjson.JSON;
import com.aurora.model.dto.ArticleSearchDTO;
import com.aurora.model.dto.MaxwellDataDTO;
import com.aurora.entity.Article;
import com.aurora.mapper.ElasticsearchMapper;
import com.aurora.util.BeanCopyUtil;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.aurora.constant.RabbitMQConstant.MAXWELL_EXCHANGE;
import static com.aurora.constant.RabbitMQConstant.MAXWELL_QUEUE;

@Component
public class MaxWellConsumer {

    @Autowired
    private ElasticsearchMapper elasticsearchMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MAXWELL_QUEUE,
                    durable = "true"),
            exchange = @Exchange(name = MAXWELL_EXCHANGE, type = ExchangeTypes.FANOUT)))
    public void process(byte[] data) {
        MaxwellDataDTO maxwellDataDTO = JSON.parseObject(new String(data), MaxwellDataDTO.class);
        Article article = JSON.parseObject(JSON.toJSONString(maxwellDataDTO.getData()), Article.class);
        switch (maxwellDataDTO.getType()) {
            case "insert":
            case "update":
                elasticsearchMapper.save(BeanCopyUtil.copyObject(article, ArticleSearchDTO.class));
                break;
            case "delete":
                elasticsearchMapper.deleteById(article.getId());
                break;
            default:
                break;
        }
    }
}