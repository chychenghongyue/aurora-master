package com.blog.mapper;

import com.blog.model.dto.ArticleSearchDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


/**
 * @author 长乐予安
 * elasticsearch
 */
@Repository
public interface ElasticsearchMapper extends ElasticsearchRepository<ArticleSearchDTO,Integer> {

}
