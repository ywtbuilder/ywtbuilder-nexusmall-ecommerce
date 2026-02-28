package com.mall.module.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.product.dto.PmsProductCategoryParam;
import com.mall.module.product.dto.PmsProductCategoryWithChildrenItem;
import com.mall.module.product.entity.PmsProductCategory;

import java.util.List;

/**
 * 商品分类服务
 */
public interface ProductCategoryService {
    List<PmsProductCategoryWithChildrenItem> listWithChildren();
    Page<PmsProductCategory> list(Long parentId, Integer pageNum, Integer pageSize);
    PmsProductCategory getItem(Long id);
    int create(PmsProductCategoryParam param);
    int update(Long id, PmsProductCategoryParam param);
    int delete(Long id);
    int updateNavStatus(List<Long> ids, Integer navStatus);
    int updateShowStatus(List<Long> ids, Integer showStatus);
}
