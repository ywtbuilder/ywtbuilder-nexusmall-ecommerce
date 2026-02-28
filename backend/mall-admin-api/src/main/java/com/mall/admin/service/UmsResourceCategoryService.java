package com.mall.admin.service;

import com.mall.admin.entity.UmsResourceCategory;

import java.util.List;

/**
 * 资源分类管理 Service
 */
public interface UmsResourceCategoryService {

    int create(UmsResourceCategory category);

    int update(Long id, UmsResourceCategory category);

    int delete(Long id);

    List<UmsResourceCategory> listAll();
}
