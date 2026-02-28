package com.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.module.product.entity.PmsAsset;
import com.mall.module.product.entity.PmsProductSpec;
import com.mall.module.product.mapper.PmsAssetMapper;
import com.mall.module.product.mapper.PmsProductImageMapper;
import com.mall.module.product.mapper.PmsProductSpecMapper;
import com.mall.module.product.service.AssetService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssetServiceImpl implements AssetService {

    private static final int ASSET_CACHE_MAX_ENTRIES = 256;
    private static final long ASSET_CACHE_TTL_MILLIS = 120_000L;

    private final Map<String, AssetCacheEntry> assetCache = Collections.synchronizedMap(
            new LinkedHashMap<>(ASSET_CACHE_MAX_ENTRIES, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, AssetCacheEntry> eldest) {
                    return size() > ASSET_CACHE_MAX_ENTRIES;
                }
            }
    );

    private static final class AssetCacheEntry {
        private final PmsAsset asset;
        private final long expireAtMillis;

        private AssetCacheEntry(PmsAsset asset, long expireAtMillis) {
            this.asset = asset;
            this.expireAtMillis = expireAtMillis;
        }

        private boolean isExpired(long nowMillis) {
            return nowMillis >= expireAtMillis;
        }
    }

    private final PmsAssetMapper assetMapper;
    private final PmsProductSpecMapper specMapper;
    private final PmsProductImageMapper productImageMapper;

    public AssetServiceImpl(PmsAssetMapper assetMapper,
                            PmsProductSpecMapper specMapper,
                            PmsProductImageMapper productImageMapper) {
        this.assetMapper = assetMapper;
        this.specMapper = specMapper;
        this.productImageMapper = productImageMapper;
    }

    @Override
    public PmsAsset getByHash(String imageHash) {
        long nowMillis = System.currentTimeMillis();
        AssetCacheEntry cached = assetCache.get(imageHash);
        if (cached != null && !cached.isExpired(nowMillis)) {
            return cached.asset;
        }

        LambdaQueryWrapper<PmsAsset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsAsset::getImageHash, imageHash);
        PmsAsset asset = assetMapper.selectOne(wrapper);
        if (asset != null) {
            assetCache.put(imageHash, new AssetCacheEntry(asset, nowMillis + ASSET_CACHE_TTL_MILLIS));
        }
        return asset;
    }

    @Override
    public List<PmsProductSpec> getSpecsByProductId(Long productId) {
        LambdaQueryWrapper<PmsProductSpec> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProductSpec::getProductId, productId);
        wrapper.orderByAsc(PmsProductSpec::getSortOrder);
        return specMapper.selectList(wrapper);
    }

    @Override
    public List<String> getImageUrlsByProductIdAndType(Long productId, Integer imageType) {
        return productImageMapper.selectImageUrlsByProductIdAndType(productId, imageType);
    }
}
