package com.mall.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsMenu;
import com.mall.admin.entity.UmsResource;
import com.mall.admin.entity.UmsRole;

import java.util.List;

/**
 * 角色管理 Service
 */
public interface UmsRoleService {

    int create(UmsRole role);

    int update(Long id, UmsRole role);

    int delete(List<Long> ids);

    Page<UmsRole> list(String keyword, Integer pageNum, Integer pageSize);

    List<UmsRole> listAll();

    int updateStatus(Long id, Integer status);

    /** 获取角色关联的资源列表 */
    List<UmsResource> listResource(Long roleId);

    /** 获取角色关联的菜单列表 */
    List<UmsMenu> listMenu(Long roleId);

    /** 分配资源 */
    int allocResource(Long roleId, List<Long> resourceIds);

    /** 分配菜单 */
    int allocMenu(Long roleId, List<Long> menuIds);
}
