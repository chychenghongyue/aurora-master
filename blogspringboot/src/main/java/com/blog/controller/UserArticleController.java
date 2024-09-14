package com.blog.controller;

import com.blog.model.dto.UserArticleDTO;
import com.blog.model.vo.ResultVO;
import com.blog.service.UserArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserArticleController {
    @Autowired
    private UserArticleService userArticleService;

    @PostMapping("/team/insert")
    public ResultVO<?> insert(@RequestBody UserArticleDTO userArticleDTO) {
        System.err.println(userArticleDTO);
        userArticleService.insert(userArticleDTO);
        return ResultVO.ok();
    }

    @DeleteMapping("/team/delete")
    public ResultVO<?> delete(@RequestBody UserArticleDTO userArticleDTO) {
        userArticleService.delete(userArticleDTO);
        return ResultVO.ok();
    }

    @GetMapping("/team/select/{userId}")
    public ResultVO<List<Integer>> select(@PathVariable("userId") String userId) {
        List<Integer> list = userArticleService.select(userId);
        return ResultVO.ok(list);
    }
}
