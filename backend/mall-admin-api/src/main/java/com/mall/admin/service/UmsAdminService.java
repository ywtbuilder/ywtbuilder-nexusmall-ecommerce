package com.mall.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsAdmin;
import com.mall.admin.entity.UmsRole;

import java.util.List;

/**
 * 后台用户管理 Service
 */
public interface UmsAdminService {

    /** 后台用户登录，返回 JWT token */
    String login(String username, String password);

    /** 注册后台用户 */
    UmsAdmin register(UmsAdmin admin);

    /** 获取用户信息（含角色、菜单、资源） */
    UmsAdmin getAdminByUsername(String username);

    /** 获取用户信息 */
    UmsAdmin getItem(Long id);

    /** 后台用户列表（分页） */
    Page<UmsAdmin> list(String keyword, Integer pageNum, Integer pageSize);

    /** 修改用户 */
    int update(Long id, UmsAdmin admin);

    /** 修改帐号状态 */
    int updateStatus(Long id, Integer status);

    /** 删除用户 */
    int delete(Long id);

    /** 分配角色 */
    int updateRole(Long adminId, List<Long> roleIds);

    /** 获取用户的角色列表 */
    List<UmsRole> getRoleList(Long adminId);

    /** 刷新 token */
    String refreshToken(String oldToken);
}
