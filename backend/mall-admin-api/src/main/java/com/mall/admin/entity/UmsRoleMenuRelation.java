package com.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色-菜单关系
 */
@Data
@TableName("ums_role_menu_relation")
public class UmsRoleMenuRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long menuId;
}
