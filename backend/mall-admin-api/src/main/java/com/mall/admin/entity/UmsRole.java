package com.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台角色
 */
@Data
@TableName("ums_role")
public class UmsRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer adminCount;
    private LocalDateTime createTime;
    /** 0->禁用；1->启用 */
    private Integer status;
    private Integer sort;
}
