package com.mall.module.product.service;

import com.mall.module.product.entity.PmsSkuStock;

import java.util.List;

/**
 * sku库存管理服务
 */
public interface SkuStockService {
    List<PmsSkuStock> getList(Long pid, String keyword);
    int update(Long pid, List<PmsSkuStock> skuStockList);
}
