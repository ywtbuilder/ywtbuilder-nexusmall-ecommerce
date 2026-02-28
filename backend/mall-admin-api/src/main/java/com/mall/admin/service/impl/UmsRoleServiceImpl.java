package com.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.*;
import com.mall.admin.mapper.*;
import com.mall.admin.service.UmsRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UmsRoleServiceImpl implements UmsRoleService {

    private final UmsRoleMapper roleMapper;
    private final UmsRoleResourceRelationMapper roleResourceRelationMapper;
    private final UmsRoleMenuRelationMapper roleMenuRelationMapper;
    private final UmsResourceMapper resourceMapper;
    private final UmsMenuMapper menuMapper;

    public UmsRoleServiceImpl(UmsRoleMapper roleMapper,
                              UmsRoleResourceRelationMapper roleResourceRelationMapper,
                              UmsRoleMenuRelationMapper roleMenuRelationMapper,
                              UmsResourceMapper resourceMapper,
                              UmsMenuMapper menuMapper) {
        this.roleMapper = roleMapper;
        this.roleResourceRelationMapper = roleResourceRelationMapper;
        this.roleMenuRelationMapper = roleMenuRelationMapper;
        this.resourceMapper = resourceMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public int create(UmsRole role) {
        role.setCreateTime(LocalDateTime.now());
        role.setAdminCount(0);
        return roleMapper.insert(role);
    }

    @Override
    public int update(Long id, UmsRole role) {
        role.setId(id);
        return roleMapper.updateById(role);
    }

    @Override
    @Transactional
    public int delete(List<Long> ids) {
        // 删除关联关系
        roleResourceRelationMapper.delete(
                new LambdaQueryWrapper<UmsRoleResourceRelation>()
                        .in(UmsRoleResourceRelation::getRoleId, ids));
        roleMenuRelationMapper.delete(
                new LambdaQueryWrapper<UmsRoleMenuRelation>()
                        .in(UmsRoleMenuRelation::getRoleId, ids));
        return roleMapper.deleteByIds(ids);
    }

    @Override
    public Page<UmsRole> list(String keyword, Integer pageNum, Integer pageSize) {
        Page<UmsRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(UmsRole::getName, keyword);
        }
        wrapper.orderByDesc(UmsRole::getSort);
        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<UmsRole> listAll() {
        return roleMapper.selectList(null);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        UmsRole role = new UmsRole();
        role.setId(id);
        role.setStatus(status);
        return roleMapper.updateById(role);
    }

    @Override
    public List<UmsResource> listResource(Long roleId) {
        List<UmsRoleResourceRelation> relations = roleResourceRelationMapper.selectList(
                new LambdaQueryWrapper<UmsRoleResourceRelation>()
                        .eq(UmsRoleResourceRelation::getRoleId, roleId));
        if (relations.isEmpty()) return List.of();
        List<Long> resourceIds = relations.stream()
                .map(UmsRoleResourceRelation::getResourceId)
                .collect(Collectors.toList());
        return resourceMapper.selectList(
                new LambdaQueryWrapper<UmsResource>().in(UmsResource::getId, resourceIds));
    }

    @Override
    public List<UmsMenu> listMenu(Long roleId) {
        List<UmsRoleMenuRelation> relations = roleMenuRelationMapper.selectList(
                new LambdaQueryWrapper<UmsRoleMenuRelation>()
                        .eq(UmsRoleMenuRelation::getRoleId, roleId));
        if (relations.isEmpty()) return List.of();
        List<Long> menuIds = relations.stream()
                .map(UmsRoleMenuRelation::getMenuId)
                .collect(Collectors.toList());
        return menuMapper.selectList(
                new LambdaQueryWrapper<UmsMenu>().in(UmsMenu::getId, menuIds));
    }

    @Override
    @Transactional
    public int allocResource(Long roleId, List<Long> resourceIds) {
        // 先删除原有关系
        roleResourceRelationMapper.delete(
                new LambdaQueryWrapper<UmsRoleResourceRelation>()
                        .eq(UmsRoleResourceRelation::getRoleId, roleId));
        // 插入新关系
        for (Long resourceId : resourceIds) {
            UmsRoleResourceRelation relation = new UmsRoleResourceRelation();
            relation.setRoleId(roleId);
            relation.setResourceId(resourceId);
            roleResourceRelationMapper.insert(relation);
        }
        return resourceIds.size();
    }

    @Override
    @Transactional
    public int allocMenu(Long roleId, List<Long> menuIds) {
        // 先删除原有关系
        roleMenuRelationMapper.delete(
                new LambdaQueryWrapper<UmsRoleMenuRelation>()
                        .eq(UmsRoleMenuRelation::getRoleId, roleId));
        // 插入新关系
        for (Long menuId : menuIds) {
            UmsRoleMenuRelation relation = new UmsRoleMenuRelation();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuRelationMapper.insert(relation);
        }
        return menuIds.size();
    }
}
