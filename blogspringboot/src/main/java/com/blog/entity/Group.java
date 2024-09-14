package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_group")
public class Group implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    public Integer id;
    public String name;
    public String ico;
    @TableLogic
    public Integer isDelete;//逻辑删除
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
