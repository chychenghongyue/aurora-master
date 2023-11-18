package com.blog.strategy.impl;

import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import com.blog.model.dto.ArticleSearchDTO;
import com.blog.strategy.SearchStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.blog.constant.CommonConstant.*;
import static com.blog.enums.ArticleStatusEnum.PUBLIC;

@Service("mySqlSearchStrategyImpl")
public class MySqlSearchStrategyImpl implements SearchStrategy {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public List<ArticleSearchDTO> searchArticle(String keywords) {
        // 如果关键字为空，直接返回空列表
        if (StringUtils.isBlank(keywords)) {
            return new ArrayList<>();
        }

        // 使用 MyBatis-Plus 提供的 LambdaQueryWrapper 构建查询条件
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDelete, FALSE)
                .eq(Article::getStatus, PUBLIC.getStatus())
                .and(i -> i.like(Article::getArticleTitle, keywords)
                        .or()
                        .like(Article::getArticleContent, keywords)));

        // 对查询结果进行处理，将结果映射为 ArticleSearchDTO 对象列表
        return articles.stream().map(item -> {
                    boolean isLowerCase = true;
                    String articleContent = item.getArticleContent();
                    // 在文章内容中查找关键字的位置
                    int contentIndex = item.getArticleContent().indexOf(keywords.toLowerCase());
                    if (contentIndex == -1) {
                        contentIndex = item.getArticleContent().indexOf(keywords.toUpperCase());
                        if (contentIndex != -1) {
                            isLowerCase = false;
                        }
                    }
                    // 如果找到关键字
                    if (contentIndex != -1) {
                        // 截取关键字前后的一定长度的文本
                        int preIndex = contentIndex > 15 ? contentIndex - 15 : 0;
                        String preText = item.getArticleContent().substring(preIndex, contentIndex);
                        int last = contentIndex + keywords.length();
                        int postLength = item.getArticleContent().length() - last;
                        int postIndex = postLength > 35 ? last + 35 : last + postLength;
                        String postText = item.getArticleContent().substring(contentIndex, postIndex);
                        // 根据关键字的大小写，替换关键字，并添加一些标签（可能是用于前端展示的样式）
                        if (isLowerCase) {
                            articleContent = (preText + postText).replaceAll(keywords.toLowerCase(), PRE_TAG + keywords.toLowerCase() + POST_TAG);
                        } else {
                            articleContent = (preText + postText).replaceAll(keywords.toUpperCase(), PRE_TAG + keywords.toUpperCase() + POST_TAG);
                        }
                    } else {
                        // 如果没有找到关键字，返回 null
                        return null;
                    }

                    // 处理文章标题中的关键字
                    isLowerCase = true;
                    int titleIndex = item.getArticleTitle().indexOf(keywords.toLowerCase());
                    if (titleIndex == -1) {
                        titleIndex = item.getArticleTitle().indexOf(keywords.toUpperCase());
                        if (titleIndex != -1) {
                            isLowerCase = false;
                        }
                    }
                    String articleTitle;
                    if (isLowerCase) {
                        articleTitle = item.getArticleTitle().replaceAll(keywords.toLowerCase(), PRE_TAG + keywords.toLowerCase() + POST_TAG);
                    } else {
                        articleTitle = item.getArticleTitle().replaceAll(keywords.toUpperCase(), PRE_TAG + keywords.toUpperCase() + POST_TAG);
                    }

                    // 构建 ArticleSearchDTO 对象并返回
                    return ArticleSearchDTO.builder()
                            .id(item.getId())
                            .articleTitle(articleTitle)
                            .articleContent(articleContent)
                            .build();
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
