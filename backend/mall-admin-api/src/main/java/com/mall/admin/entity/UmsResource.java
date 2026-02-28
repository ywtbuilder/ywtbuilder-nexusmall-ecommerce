package com.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台资源（接口/URL）
 */
@Data
@TableName("ums_resource")
public class UmsResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDateTime createTime;
    /** 资源名称 */
    private String name;
    /** 资源 URL Pattern */
    private String url;
    /** 描述 */
    private String description;
    /** 资源分类ID */
    private Long categoryId;
}
