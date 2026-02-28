package com.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsResource;
import com.mall.admin.mapper.UmsResourceMapper;
import com.mall.admin.service.UmsResourceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UmsResourceServiceImpl implements UmsResourceService {

    private final UmsResourceMapper resourceMapper;

    public UmsResourceServiceImpl(UmsResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
    }

    @Override
    public int create(UmsResource resource) {
        resource.setCreateTime(LocalDateTime.now());
        return resourceMapper.insert(resource);
    }

    @Override
    public int update(Long id, UmsResource resource) {
        resource.setId(id);
        return resourceMapper.updateById(resource);
    }

    @Override
    public int delete(Long id) {
        return resourceMapper.deleteById(id);
    }

    @Override
    public Page<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword,
                                   Integer pageNum, Integer pageSize) {
        Page<UmsResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsResource> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(UmsResource::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(nameKeyword)) {
            wrapper.like(UmsResource::getName, nameKeyword);
        }
        if (StringUtils.hasText(urlKeyword)) {
            wrapper.like(UmsResource::getUrl, urlKeyword);
        }
        wrapper.orderByDesc(UmsResource::getCreateTime);
        return resourceMapper.selectPage(page, wrapper);
    }

    @Override
    public List<UmsResource> listAll() {
        return resourceMapper.selectList(null);
    }

    @Override
    public UmsResource getItem(Long id) {
        return resourceMapper.selectById(id);
    }
}
