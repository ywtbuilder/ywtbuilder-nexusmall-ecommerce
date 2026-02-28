package com.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.module.product.entity.PmsSkuStock;
import com.mall.module.product.mapper.PmsSkuStockMapper;
import com.mall.module.product.service.SkuStockService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SkuStockServiceImpl implements SkuStockService {

    private final PmsSkuStockMapper skuStockMapper;

    public SkuStockServiceImpl(PmsSkuStockMapper skuStockMapper) {
        this.skuStockMapper = skuStockMapper;
    }

    @Override
    public List<PmsSkuStock> getList(Long pid, String keyword) {
        LambdaQueryWrapper<PmsSkuStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsSkuStock::getProductId, pid);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(PmsSkuStock::getSkuCode, keyword);
        }
        return skuStockMapper.selectList(wrapper);
    }

    @Override
    public int update(Long pid, List<PmsSkuStock> skuStockList) {
        // 删除旧sku, 插入新sku
        skuStockMapper.delete(new LambdaQueryWrapper<PmsSkuStock>().eq(PmsSkuStock::getProductId, pid));
        for (PmsSkuStock skuStock : skuStockList) {
            skuStock.setProductId(pid);
            skuStock.setId(null);
            skuStockMapper.insert(skuStock);
        }
        return skuStockList.size();
    }
}
