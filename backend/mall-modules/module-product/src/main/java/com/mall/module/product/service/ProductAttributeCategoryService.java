package com.mall.module.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.product.dto.PmsProductAttributeCategoryItem;
import com.mall.module.product.entity.PmsProductAttributeCategory;

import java.util.List;

/**
 * 商品属性分类服务
 */
public interface ProductAttributeCategoryService {
    List<PmsProductAttributeCategoryItem> getListWithAttr();
    Page<PmsProductAttributeCategory> list(Integer pageNum, Integer pageSize);
    int create(String name);
    int update(Long id, String name);
    int delete(Long id);
}
