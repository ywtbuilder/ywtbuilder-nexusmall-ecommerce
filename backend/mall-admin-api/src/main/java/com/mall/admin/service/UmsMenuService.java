package com.mall.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsMenu;

import java.util.List;

/**
 * 菜单管理 Service
 */
public interface UmsMenuService {

    int create(UmsMenu menu);

    int update(Long id, UmsMenu menu);

    int delete(Long id);

    /** 分页获取菜单列表 */
    Page<UmsMenu> list(Long parentId, Integer pageNum, Integer pageSize);

    /** 根据ID获取菜单 */
    UmsMenu getItem(Long id);

    /** 树形菜单列表 */
    List<UmsMenu> treeList();

    /** 修改菜单显示状态 */
    int updateHidden(Long id, Integer hidden);
}
