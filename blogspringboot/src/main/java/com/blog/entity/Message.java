package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_message")
public class Message {
    @TableId(value = "id", type = IdType.AUTO)
    public Integer id;
    public Integer groupId;
    public Integer userId;
    public Integer replyMessageId;
    public String text;
    public String pictureUrl;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
