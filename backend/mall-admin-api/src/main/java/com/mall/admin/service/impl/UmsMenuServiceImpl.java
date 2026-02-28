package com.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsMenu;
import com.mall.admin.mapper.UmsMenuMapper;
import com.mall.admin.service.UmsMenuService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UmsMenuServiceImpl implements UmsMenuService {

    private final UmsMenuMapper menuMapper;

    public UmsMenuServiceImpl(UmsMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public int create(UmsMenu menu) {
        menu.setCreateTime(LocalDateTime.now());
        updateLevel(menu);
        return menuMapper.insert(menu);
    }

    @Override
    public int update(Long id, UmsMenu menu) {
        menu.setId(id);
        updateLevel(menu);
        return menuMapper.updateById(menu);
    }

    @Override
    public int delete(Long id) {
        return menuMapper.deleteById(id);
    }

    @Override
    public Page<UmsMenu> list(Long parentId, Integer pageNum, Integer pageSize) {
        Page<UmsMenu> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsMenu::getParentId, parentId)
               .orderByDesc(UmsMenu::getSort);
        return menuMapper.selectPage(page, wrapper);
    }

    @Override
    public UmsMenu getItem(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    public List<UmsMenu> treeList() {
        List<UmsMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<UmsMenu>().orderByDesc(UmsMenu::getSort));
        // 构建树形结构
        List<UmsMenu> rootMenus = allMenus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());
        rootMenus.forEach(root -> buildChildren(root, allMenus));
        return rootMenus;
    }

    @Override
    public int updateHidden(Long id, Integer hidden) {
        UmsMenu menu = new UmsMenu();
        menu.setId(id);
        menu.setHidden(hidden);
        return menuMapper.updateById(menu);
    }

    private void updateLevel(UmsMenu menu) {
        if (menu.getParentId() == null || menu.getParentId() == 0) {
            menu.setLevel(0);
        } else {
            UmsMenu parentMenu = menuMapper.selectById(menu.getParentId());
            if (parentMenu != null) {
                menu.setLevel(parentMenu.getLevel() + 1);
            } else {
                menu.setLevel(0);
            }
        }
    }

    private void buildChildren(UmsMenu parent, List<UmsMenu> allMenus) {
        List<UmsMenu> children = allMenus.stream()
                .filter(m -> parent.getId().equals(m.getParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        children.forEach(child -> buildChildren(child, allMenus));
    }
}
