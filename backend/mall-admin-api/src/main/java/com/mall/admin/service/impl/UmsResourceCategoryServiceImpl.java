package com.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.admin.entity.UmsResourceCategory;
import com.mall.admin.mapper.UmsResourceCategoryMapper;
import com.mall.admin.service.UmsResourceCategoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UmsResourceCategoryServiceImpl implements UmsResourceCategoryService {

    private final UmsResourceCategoryMapper categoryMapper;

    public UmsResourceCategoryServiceImpl(UmsResourceCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public int create(UmsResourceCategory category) {
        category.setCreateTime(LocalDateTime.now());
        return categoryMapper.insert(category);
    }

    @Override
    public int update(Long id, UmsResourceCategory category) {
        category.setId(id);
        return categoryMapper.updateById(category);
    }

    @Override
    public int delete(Long id) {
        return categoryMapper.deleteById(id);
    }

    @Override
    public List<UmsResourceCategory> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<UmsResourceCategory>()
                        .orderByDesc(UmsResourceCategory::getSort));
    }
}
