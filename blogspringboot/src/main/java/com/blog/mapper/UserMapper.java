package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.User;
import com.blog.model.vo.ResultVO;

public interface UserMapper extends BaseMapper<User> {
    ResultVO ziLogin(String username, String password);
}
