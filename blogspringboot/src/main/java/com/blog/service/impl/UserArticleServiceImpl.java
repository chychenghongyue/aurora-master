package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.entity.UserArticle;
import com.blog.mapper.UserArticleMapper;
import com.blog.model.dto.UserArticleDTO;
import com.blog.service.UserArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserArticleServiceImpl implements UserArticleService {
    @Autowired
    private UserArticleMapper userArticleMapper;

    @Override
    public void insert(UserArticleDTO userArticleDTO) {
        List<Integer> list = userArticleDTO.getUserId();
        for (Integer i : list) {
            UserArticle userArticle = UserArticle.builder()
                    .creatId(userArticleDTO.getCreatId())
                    .userId(i)
                    .articleId(userArticleDTO.getArticleId())
                    .build();
            QueryWrapper<UserArticle> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("creat_id", userArticleDTO.getCreatId())
                    .eq("user_id", i)
                    .eq("article_id", userArticleDTO.getArticleId());
            Integer flag = userArticleMapper.selectCount(queryWrapper);
            if (flag == 1) {
                continue;
            }
            userArticleMapper.insert(userArticle);
        }
    }

    @Override
    public void delete(UserArticleDTO userArticleDTO) {
        QueryWrapper<UserArticle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id", userArticleDTO.getArticleId());
        userArticleMapper.delete(queryWrapper);
    }

    @Override
    public List<Integer> select(String userId) {
        QueryWrapper<UserArticle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<UserArticle> userArticles = userArticleMapper.selectList(queryWrapper);
        List<Integer> list = new ArrayList<>();
        for (UserArticle userArticle : userArticles) {
            list.add(userArticle.getArticleId());
        }
        return list;
    }
}
