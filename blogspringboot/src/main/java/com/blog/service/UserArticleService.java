package com.blog.service;

import com.blog.model.dto.UserArticleDTO;

import java.util.List;

public interface UserArticleService {
    void insert(UserArticleDTO userArticleDTO);

    void delete(UserArticleDTO userArticleDTO);

    List<Integer> select(String userId);
}
