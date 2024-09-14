package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {
    @TableId(value = "id",type = IdType.INPUT)
    private Integer id;
    private String name;
    private String nickName;
    private String password;
    private String mobile;
    private Integer onlineStatus;
    private String avatar;
    private String email;
    private String sessionHistory;
    private String chatId;
    private String timestamp;
}
