package com.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.*;
import com.blog.enums.FileExtEnum;
import com.blog.enums.FilePathEnum;
import com.blog.exception.BizException;
import com.blog.mapper.*;
import com.blog.model.dto.*;
import com.blog.model.vo.*;
import com.blog.service.ArticleService;
import com.blog.service.ArticleTagService;
import com.blog.service.RedisService;
import com.blog.service.TagService;
import com.blog.strategy.context.SearchStrategyContext;
import com.blog.strategy.context.UploadStrategyContext;
import com.blog.util.BeanCopyUtil;
import com.blog.util.PageUtil;
import com.blog.util.UserUtil;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.blog.constant.RabbitMQConstant.ELASTIC_EXCHANGE;
import static com.blog.constant.RabbitMQConstant.SUBSCRIBE_EXCHANGE;
import static com.blog.constant.RedisConstant.ARTICLE_ACCESS;
import static com.blog.constant.RedisConstant.ARTICLE_VIEWS_COUNT;
import static com.blog.enums.ArticleStatusEnum.DRAFT;
import static com.blog.enums.StatusCodeEnum.ARTICLE_ACCESS_FAIL;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private UserArticleMapper userArticleMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAuthMapper userAuthMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private TagService tagService;

    @Autowired
    private ArticleTagService articleTagService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UploadStrategyContext uploadStrategyContext;

    @Autowired
    private SearchStrategyContext searchStrategyContext;

    @SneakyThrows
    @Override
    public TopAndFeaturedArticlesDTO listTopAndFeaturedArticles() {
        List<ArticleCardDTO> articleCardDTOs = articleMapper.listTopAndFeaturedArticles();
        if (articleCardDTOs.size() == 0) {
            return new TopAndFeaturedArticlesDTO();
        } else if (articleCardDTOs.size() > 3) {
            articleCardDTOs = articleCardDTOs.subList(0, 3);
        }
        articleCardDTOs.forEach(item -> {
            UserAuth userAuth = userAuthMapper.selectById(item.getUserId());
            UserInfo userInfo = userInfoMapper.selectById(userAuth.getUserInfoId());
            item.setAuthor(userInfo);
        });
        TopAndFeaturedArticlesDTO topAndFeaturedArticlesDTO = new TopAndFeaturedArticlesDTO();
        topAndFeaturedArticlesDTO.setTopArticle(articleCardDTOs.get(0));
        articleCardDTOs.remove(0);
        topAndFeaturedArticlesDTO.setFeaturedArticles(articleCardDTOs);
        return topAndFeaturedArticlesDTO;
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArticleCardDTO> listArticles() {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDelete, 0)
                .in(Article::getStatus, 1, 2);
        CompletableFuture<Integer> asyncCount = CompletableFuture.supplyAsync(() -> Math.toIntExact(articleMapper.selectCount(queryWrapper)));
        List<ArticleCardDTO> articles = articleMapper.listArticles(PageUtil.getLimitCurrent(), PageUtil.getSize());
        articles.forEach(item -> {
            UserAuth userAuth = userAuthMapper.selectById(item.getUserId());
            UserInfo userInfo = userInfoMapper.selectById(userAuth.getUserInfoId());
            item.setAuthor(userInfo);
        });
        return new PageResultDTO<>(articles, asyncCount.get());
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArticleCardDTO> listArticlesByCategoryId(Integer categoryId) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, categoryId);
        CompletableFuture<Integer> asyncCount = CompletableFuture.supplyAsync(() -> Math.toIntExact(articleMapper.selectCount(queryWrapper)));
        List<ArticleCardDTO> articles = articleMapper.getArticlesByCategoryId(PageUtil.getLimitCurrent(), PageUtil.getSize(), categoryId);
        articles.forEach(item -> {
            UserAuth userAuth = userAuthMapper.selectById(item.getUserId());
            UserInfo userInfo = new UserInfo();
            if (userAuth != null)
                userInfo = userInfoMapper.selectById(userAuth.getUserInfoId());
            item.setAuthor(userInfo);
        });
        return new PageResultDTO<>(articles, asyncCount.get());
    }

    @SneakyThrows
    @Override
    public ArticleDTO getArticleById(Integer articleId) {
        Article articleForCheck = articleMapper.selectOne(new LambdaQueryWrapper<Article>().eq(Article::getId, articleId));
        if (Objects.isNull(articleForCheck)) {
            return null;
        }
        if (articleForCheck.getStatus().equals(2)) {
            Boolean isAccess;
            try {
                isAccess = redisService.sIsMember(ARTICLE_ACCESS + UserUtil.getUserDetailsDTO().getId(), articleId);
            } catch (Exception exception) {
                throw new BizException(ARTICLE_ACCESS_FAIL);
            }
            if (isAccess.equals(false)) {
                throw new BizException(ARTICLE_ACCESS_FAIL);
            }
        }
        updateArticleViewsCount(articleId);
        CompletableFuture<ArticleDTO> asyncArticle = CompletableFuture.supplyAsync(() -> articleMapper.getArticleById(articleId));
        CompletableFuture<ArticleCardDTO> asyncPreArticle = CompletableFuture.supplyAsync(() -> {
            ArticleCardDTO preArticle = articleMapper.getPreArticleById(articleId);
            if (Objects.isNull(preArticle)) {
                preArticle = articleMapper.getLastArticle();
                UserAuth userAuth = userAuthMapper.selectById(preArticle.getUserId());
                UserInfo userInfo = userInfoMapper.selectById(userAuth.getUserInfoId());
                preArticle.setAuthor(userInfo);
            }
            return preArticle;
        });
        CompletableFuture<ArticleCardDTO> asyncNextArticle = CompletableFuture.supplyAsync(() -> {
            ArticleCardDTO nextArticle = articleMapper.getNextArticleById(articleId);
            if (Objects.isNull(nextArticle)) {
                nextArticle = articleMapper.getFirstArticle();
                UserAuth userAuth = userAuthMapper.selectById(nextArticle.getUserId());
                UserInfo userInfo = userInfoMapper.selectById(userAuth.getUserInfoId());
                nextArticle.setAuthor(userInfo);
            }
            return nextArticle;
        });
        ArticleDTO article = asyncArticle.get();
        if (Objects.isNull(article)) {
            return null;
        }
        UserAuth userAuth = userAuthMapper.selectById(article.getUserId());
        UserInfo userInfo = userInfoMapper.selectById(userAuth.getUserInfoId());
        article.setAuthor(userInfo);
        Double score = redisService.zScore(ARTICLE_VIEWS_COUNT, articleId);
        if (Objects.nonNull(score)) {
            article.setViewCount(score.intValue());
        }
        article.setPreArticleCard(asyncPreArticle.get());
        article.setNextArticleCard(asyncNextArticle.get());

        return article;
    }

    @Override
    public void accessArticle(ArticlePasswordVO articlePasswordVO) {
        Article article = articleMapper.selectOne(new LambdaQueryWrapper<Article>().eq(Article::getId, articlePasswordVO.getArticleId()));
        if (Objects.isNull(article)) {
            throw new BizException("文章不存在");
        }
        if (article.getPassword().equals(articlePasswordVO.getArticlePassword())) {
            redisService.sAdd(ARTICLE_ACCESS + UserUtil.getUserDetailsDTO().getId(), articlePasswordVO.getArticleId());
        } else {
            throw new BizException("密码错误");
        }
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArticleCardDTO> listArticlesByTagId(Integer tagId) {
        LambdaQueryWrapper<ArticleTag> queryWrapper = new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tagId);
        CompletableFuture<Integer> asyncCount = CompletableFuture.supplyAsync(() -> Math.toIntExact(articleTagMapper.selectCount(queryWrapper)));
        List<ArticleCardDTO> articles = articleMapper.listArticlesByTagId(PageUtil.getLimitCurrent(), PageUtil.getSize(), tagId);
        articles.forEach(item -> {
            UserAuth userAuth = userAuthMapper.selectById(item.getUserId());
            UserInfo userInfo = userInfoMapper.selectById(userAuth.getUserInfoId());
            item.setAuthor(userInfo);
        });
        return new PageResultDTO<>(articles, asyncCount.get());
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArchiveDTO> listArchives() {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<Article>().eq(Article::getIsDelete, 0).eq(Article::getStatus, 1);
        CompletableFuture<Integer> asyncCount = CompletableFuture.supplyAsync(() -> Math.toIntExact(articleMapper.selectCount(queryWrapper)));
        List<ArticleCardDTO> articles = articleMapper.listArchives(PageUtil.getLimitCurrent(), PageUtil.getSize());
        HashMap<String, List<ArticleCardDTO>> map = new HashMap<>();
        for (ArticleCardDTO article : articles) {
            LocalDateTime createTime = article.getCreateTime();
            int month = createTime.getMonth().getValue();
            int year = createTime.getYear();
            String key = year + "-" + month;
            if (Objects.isNull(map.get(key))) {
                List<ArticleCardDTO> articleCardDTOS = new ArrayList<>();
                articleCardDTOS.add(article);
                map.put(key, articleCardDTOS);
            } else {
                map.get(key).add(article);
            }
        }
        List<ArchiveDTO> archiveDTOs = new ArrayList<>();
        map.forEach((key, value) -> archiveDTOs.add(ArchiveDTO.builder().Time(key).articles(value).build()));
        archiveDTOs.sort((o1, o2) -> {
            String[] o1s = o1.getTime().split("-");
            String[] o2s = o2.getTime().split("-");
            int o1Year = Integer.parseInt(o1s[0]);
            int o1Month = Integer.parseInt(o1s[1]);
            int o2Year = Integer.parseInt(o2s[0]);
            int o2Month = Integer.parseInt(o2s[1]);
            if (o1Year > o2Year) {
                return -1;
            } else if (o1Year < o2Year) {
                return 1;
            } else return Integer.compare(o2Month, o1Month);
        });
        return new PageResultDTO<>(archiveDTOs, asyncCount.get());
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArticleAdminDTO> listArticlesAdmin(ConditionVO conditionVO) {
        CompletableFuture<Integer> asyncCount = CompletableFuture.supplyAsync(() -> articleMapper.countArticleAdmins(conditionVO));
        List<ArticleAdminDTO> articleAdminDTOs = articleMapper.listArticlesAdmin(PageUtil.getLimitCurrent(), PageUtil.getSize(), conditionVO);
        Map<Object, Double> viewsCountMap = redisService.zAllScore(ARTICLE_VIEWS_COUNT);
        articleAdminDTOs.forEach(item -> {
            Double viewsCount = viewsCountMap.get(item.getId());
            if (Objects.nonNull(viewsCount)) {
                item.setViewsCount(viewsCount.intValue());
            }
        });
        return new PageResultDTO<>(articleAdminDTOs, asyncCount.get());
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArticleAdminDTO> listArticlesByUserId(ConditionVO conditionVO) {
        int userId = UserUtil.getUserDetailsDTO().getId();
        System.err.println("userId" + userId);
        CompletableFuture<Integer> asyncCount = CompletableFuture.supplyAsync(() -> articleMapper.countArticleById(conditionVO, userId));
        List<ArticleAdminDTO> articleAdminDTOs = articleMapper.selectByUserId(conditionVO, userId);
        Map<Object, Double> viewsCountMap = redisService.zAllScore(ARTICLE_VIEWS_COUNT);
        System.err.println("articleAdminDTOs" + articleAdminDTOs);

        QueryWrapper<UserArticle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<UserArticle> userArticles = userArticleMapper.selectList(queryWrapper);
        int count = userArticles.size() + asyncCount.get();
        System.err.println("userArticles" + userArticles);
        articleAdminDTOs.forEach(item -> {
            item.setUserId(userId);
            Double viewsCount = viewsCountMap.get(item.getId());
            if (Objects.nonNull(viewsCount)) {
                item.setViewsCount(viewsCount.intValue());
            }
        });
        List<Article> articles = new ArrayList<>();
        if (!userArticles.isEmpty()) {
            userArticles.forEach(item -> {
                Article article = articleMapper.selectById(item.getArticleId());
                article.setUserId(item.getCreatId());
                articles.add(article);
            });
            System.err.println("articles" + articles);
            if (!articles.isEmpty()) {
                for (Article item : articles) {
                    articleAdminDTOs.add(ArticleAdminDTO.builder()
                            .id(item.getId())
                            .userId(item.getUserId())
                            .status(item.getStatus())
                            .type(item.getType())
                            .articleTitle(item.getArticleTitle())
                            .isDelete(item.getIsDelete())
                            .createTime(item.getCreateTime())
                            .articleCover(item.getArticleCover())
                            .build());
                }
            }
        }
        int currentPage = (conditionVO.getCurrent().intValue() - 1) * conditionVO.getSize().intValue();
        int sizeTemp = conditionVO.getSize().intValue();
        int size = Math.min(currentPage + sizeTemp, articleAdminDTOs.size());
        List<ArticleAdminDTO> articleAdminPage = new ArrayList<>(articleAdminDTOs.subList(currentPage, size));
        return new PageResultDTO<>(articleAdminPage, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateArticle(ArticleVO articleVO) {
        System.err.println("文章自动更新：" + articleVO);
        Article article = BeanCopyUtil.copyObject(articleVO, Article.class);
        if (articleVO.getCategoryName() != null) {
            Category category = saveArticleCategory(articleVO);
            if (Objects.nonNull(category)) {
                article.setCategoryId(category.getId());
            }
        }
//        article.setUserId(UserUtil.getUserDetailsDTO().getId());
        this.saveOrUpdate(article);
        saveArticleTag(articleVO, article.getId());
        if (article.getStatus().equals(1)) {
            ArticleSearchDTO articleSearchDTO = new ArticleSearchDTO();
            BeanUtils.copyProperties(article, articleSearchDTO);
            articleSearchDTO.setIsDelete(1);
            Map<String, Object> map = new HashMap<>();
            map.put("data", articleSearchDTO);
            map.put("info", "insertOrUpDate");
            rabbitTemplate.convertAndSend(ELASTIC_EXCHANGE, map);
            log.error("发送到elastic");
            rabbitTemplate.convertAndSend(SUBSCRIBE_EXCHANGE, new Message(JSON.toJSONBytes(article.getId()), new MessageProperties()));
        }
    }

    @Override
    public void updateArticleTopAndFeatured(ArticleTopFeaturedVO articleTopFeaturedVO) {
        Article article = Article.builder()
                .id(articleTopFeaturedVO.getId())
                .isTop(articleTopFeaturedVO.getIsTop())
                .isFeatured(articleTopFeaturedVO.getIsFeatured())
                .build();
        articleMapper.updateById(article);
    }

    @Override
    public void updateArticleDelete(DeleteVO deleteVO) {
        List<Article> articles = deleteVO.getIds().stream()
                .map(id -> Article.builder()
                        .id(id)
                        .isDelete(deleteVO.getIsDelete())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(articles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticles(List<Integer> articleIds) {
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .in(ArticleTag::getArticleId, articleIds));
        articleMapper.deleteBatchIds(articleIds);
        Map<String, Object> map = new HashMap<>();
        map.put("data", articleIds);
        map.put("info", "insertOrUpDate");
        rabbitTemplate.convertAndSend(ELASTIC_EXCHANGE, map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleAdminViewDTO getArticleByIdAdmin(Integer articleId) {
        Article article = articleMapper.selectById(articleId);
        System.err.println("跳转" + article);
        Category category = categoryMapper.selectById(article.getCategoryId());
        String categoryName = null;
        if (Objects.nonNull(category)) {
            categoryName = category.getCategoryName();
        }
        List<String> tagNames = tagMapper.listTagNamesByArticleId(articleId);
        ArticleAdminViewDTO articleAdminViewDTO = BeanCopyUtil.copyObject(article, ArticleAdminViewDTO.class);
        articleAdminViewDTO.setCategoryName(categoryName);
        articleAdminViewDTO.setTagNames(tagNames);
        return articleAdminViewDTO;
    }

    @Override
    public List<String> exportArticles(List<Integer> articleIds) {
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .select(Article::getArticleTitle, Article::getArticleContent)
                .in(Article::getId, articleIds));
        List<String> urls = new ArrayList<>();
        for (Article article : articles) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(article.getArticleContent().getBytes())) {
                String url = uploadStrategyContext.executeUploadStrategy(article.getArticleTitle() + FileExtEnum.MD.getExtName(), inputStream, FilePathEnum.MD.getPath());
                urls.add(url);
            } catch (Exception e) {
                e.printStackTrace();
                throw new BizException("导出文章失败");
            }
        }
        return urls;
    }

    @Override
    public List<ArticleSearchDTO> listArticlesBySearch(ConditionVO condition) {
        return searchStrategyContext.executeSearchStrategy(condition.getKeywords());
    }


    public void updateArticleViewsCount(Integer articleId) {
        redisService.zIncr(ARTICLE_VIEWS_COUNT, articleId, 1D);
    }

    private Category saveArticleCategory(ArticleVO articleVO) {
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryName, articleVO.getCategoryName()));
        if (Objects.isNull(category) && !articleVO.getStatus().equals(DRAFT.getStatus())) {
            category = Category.builder()
                    .categoryName(articleVO.getCategoryName())
                    .build();
            if (category.getCategoryName() != null)
                categoryMapper.insert(category);
        }
        return category;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveArticleTag(ArticleVO articleVO, Integer articleId) {
        if (Objects.nonNull(articleVO.getId())) {
            articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                    .eq(ArticleTag::getArticleId, articleVO.getId()));
        }
        List<String> tagNames = articleVO.getTagNames();
        if (CollectionUtils.isNotEmpty(tagNames)) {
            List<Tag> existTags = tagService.list(new LambdaQueryWrapper<Tag>()
                    .in(Tag::getTagName, tagNames));
            List<String> existTagNames = existTags.stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());
            List<Integer> existTagIds = existTags.stream()
                    .map(Tag::getId)
                    .collect(Collectors.toList());
            tagNames.removeAll(existTagNames);
            if (CollectionUtils.isNotEmpty(tagNames)) {
                List<Tag> tags = tagNames.stream().map(item -> Tag.builder()
                                .tagName(item)
                                .build())
                        .collect(Collectors.toList());
                tagService.saveBatch(tags);
                List<Integer> tagIds = tags.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toList());
                existTagIds.addAll(tagIds);
            }
            List<ArticleTag> articleTags = existTagIds.stream().map(item -> ArticleTag.builder()
                            .articleId(articleId)
                            .tagId(item)
                            .build())
                    .collect(Collectors.toList());
            articleTagService.saveBatch(articleTags);
        }
    }

}
