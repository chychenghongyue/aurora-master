package com.blog.consumer;

import com.blog.mapper.ElasticsearchMapper;
import com.blog.model.dto.ArticleSearchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.blog.constant.RabbitMQConstant.ELASTIC_EXCHANGE;
import static com.blog.constant.RabbitMQConstant.ELASTIC_QUEUE;

@Component
@Slf4j
public class ElasticSearchConsumer {

    @Autowired
    private ElasticsearchMapper elasticsearchMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = ELASTIC_QUEUE,
                    durable = "true"),
            exchange = @Exchange(name = ELASTIC_EXCHANGE, type = ExchangeTypes.FANOUT)))
    public void process(Map<String, Object> map) {

        switch (String.valueOf(map.get("info"))) {
            case "insertOrUpDate": {
                ArticleSearchDTO articleSearchDTO = new ArticleSearchDTO();
                BeanUtils.copyProperties(map.get("data"), articleSearchDTO);
                elasticsearchMapper.save(articleSearchDTO);
                log.error("rabbit到elastic,insertOrUpDate");
                break;
            }
            case "delete": {
                List<Integer> articleIds = new ArrayList<>();
                BeanUtils.copyProperties(map.get("data"), articleIds);
                articleIds.forEach(temp -> {
                    elasticsearchMapper.deleteById(temp);
                });
                log.error("rabbit到elastic,delete");
                break;
            }
            default:
                break;
        }
    }
}