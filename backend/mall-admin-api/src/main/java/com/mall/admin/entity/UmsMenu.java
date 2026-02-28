package com.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台菜单
 */
@Data
@TableName("ums_menu")
public class UmsMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 父级ID */
    private Long parentId;
    private LocalDateTime createTime;
    /** 菜单名称 */
    private String title;
    /** 菜单级数 */
    private Integer level;
    /** 排序 */
    private Integer sort;
    /** 前端路由 name */
    private String name;
    /** 前端图标 */
    private String icon;
    /** 是否隐藏 */
    private Integer hidden;

    @TableField(exist = false)
    private List<UmsMenu> children;
}
