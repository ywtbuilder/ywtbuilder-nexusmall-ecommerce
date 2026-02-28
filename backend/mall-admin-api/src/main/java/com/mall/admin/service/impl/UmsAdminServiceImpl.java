package com.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsAdmin;
import com.mall.admin.entity.UmsAdminRoleRelation;
import com.mall.admin.entity.UmsRole;
import com.mall.admin.mapper.UmsAdminMapper;
import com.mall.admin.mapper.UmsAdminRoleRelationMapper;
import com.mall.admin.mapper.UmsRoleMapper;
import com.mall.admin.service.UmsAdminService;
import com.mall.security.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UmsAdminServiceImpl implements UmsAdminService {

    private final UmsAdminMapper adminMapper;
    private final UmsRoleMapper roleMapper;
    private final UmsAdminRoleRelationMapper adminRoleRelationMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UmsAdminServiceImpl(UmsAdminMapper adminMapper,
                               UmsRoleMapper roleMapper,
                               UmsAdminRoleRelationMapper adminRoleRelationMapper,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService) {
        this.adminMapper = adminMapper;
        this.roleMapper = roleMapper;
        this.adminRoleRelationMapper = adminRoleRelationMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String login(String username, String password) {
        UmsAdmin admin = getAdminByUsername(username);
        if (admin == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new RuntimeException("帐号已被禁用");
        }
        // 更新登录时间
        UmsAdmin updateAdmin = new UmsAdmin();
        updateAdmin.setId(admin.getId());
        updateAdmin.setLoginTime(LocalDateTime.now());
        adminMapper.updateById(updateAdmin);
        return jwtService.generateToken(admin.getUsername());
    }

    @Override
    @Transactional
    public UmsAdmin register(UmsAdmin admin) {
        // 检查用户名唯一
        UmsAdmin existing = getAdminByUsername(admin.getUsername());
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }
        admin.setCreateTime(LocalDateTime.now());
        admin.setStatus(1);
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        adminMapper.insert(admin);
        return admin;
    }

    @Override
    public UmsAdmin getAdminByUsername(String username) {
        List<UmsAdmin> admins = adminMapper.selectList(
                new LambdaQueryWrapper<UmsAdmin>().eq(UmsAdmin::getUsername, username));
        return admins.isEmpty() ? null : admins.get(0);
    }

    @Override
    public UmsAdmin getItem(Long id) {
        return adminMapper.selectById(id);
    }

    @Override
    public Page<UmsAdmin> list(String keyword, Integer pageNum, Integer pageSize) {
        Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsAdmin> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(UmsAdmin::getUsername, keyword)
                    .or().like(UmsAdmin::getNickName, keyword);
        }
        wrapper.orderByDesc(UmsAdmin::getCreateTime);
        return adminMapper.selectPage(page, wrapper);
    }

    @Override
    public int update(Long id, UmsAdmin admin) {
        admin.setId(id);
        // 如果传了密码则加密
        if (StringUtils.hasText(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        } else {
            admin.setPassword(null); // 不更新密码
        }
        return adminMapper.updateById(admin);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        UmsAdmin admin = new UmsAdmin();
        admin.setId(id);
        admin.setStatus(status);
        return adminMapper.updateById(admin);
    }

    @Override
    public int delete(Long id) {
        // 同时删除关联关系
        adminRoleRelationMapper.delete(
                new LambdaQueryWrapper<UmsAdminRoleRelation>()
                        .eq(UmsAdminRoleRelation::getAdminId, id));
        return adminMapper.deleteById(id);
    }

    @Override
    @Transactional
    public int updateRole(Long adminId, List<Long> roleIds) {
        // 先删除原有关系
        adminRoleRelationMapper.delete(
                new LambdaQueryWrapper<UmsAdminRoleRelation>()
                        .eq(UmsAdminRoleRelation::getAdminId, adminId));
        // 再插入新关系
        for (Long roleId : roleIds) {
            UmsAdminRoleRelation relation = new UmsAdminRoleRelation();
            relation.setAdminId(adminId);
            relation.setRoleId(roleId);
            adminRoleRelationMapper.insert(relation);
        }
        return roleIds.size();
    }

    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        List<UmsAdminRoleRelation> relations = adminRoleRelationMapper.selectList(
                new LambdaQueryWrapper<UmsAdminRoleRelation>()
                        .eq(UmsAdminRoleRelation::getAdminId, adminId));
        if (relations.isEmpty()) return List.of();
        List<Long> roleIds = relations.stream()
                .map(UmsAdminRoleRelation::getRoleId)
                .collect(Collectors.toList());
        return roleMapper.selectList(
                new LambdaQueryWrapper<UmsRole>().in(UmsRole::getId, roleIds));
    }

    @Override
    public String refreshToken(String oldToken) {
        return jwtService.refreshToken(oldToken);
    }
}
