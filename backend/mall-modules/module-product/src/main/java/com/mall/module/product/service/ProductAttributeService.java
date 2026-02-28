package com.mall.module.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.product.entity.PmsProductAttribute;

import java.util.List;

/**
 * 商品属性服务
 */
public interface ProductAttributeService {
    Page<PmsProductAttribute> list(Long cid, Integer type, Integer pageNum, Integer pageSize);
    PmsProductAttribute getItem(Long id);
    int create(PmsProductAttribute attr);
    int update(Long id, PmsProductAttribute attr);
    int delete(List<Long> ids);
    /** 根据分类获取属性和参数 */
    List<PmsProductAttribute> getProductAttrInfo(Long productCategoryId);
}
