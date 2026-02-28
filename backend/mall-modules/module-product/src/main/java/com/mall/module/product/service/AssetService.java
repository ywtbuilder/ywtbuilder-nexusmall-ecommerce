package com.mall.module.product.service;

import com.mall.module.product.entity.PmsAsset;
import com.mall.module.product.entity.PmsProductSpec;

import java.util.List;

/**
 * 图片资源服务
 */
public interface AssetService {
    /**
     * 根据 SHA-256 hash 获取图片资源
     */
    PmsAsset getByHash(String imageHash);

    /**
     * 获取商品的所有规格参数
     */
    List<PmsProductSpec> getSpecsByProductId(Long productId);

    /**
     * 按图片类型获取商品图片 URL（/api/asset/image/{hash}）
     */
    List<String> getImageUrlsByProductIdAndType(Long productId, Integer imageType);
}
