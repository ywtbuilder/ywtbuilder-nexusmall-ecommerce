package com.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源分类
 */
@Data
@TableName("ums_resource_category")
public class UmsResourceCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDateTime createTime;
    /** 分类名称 */
    private String name;
    private Integer sort;
}
