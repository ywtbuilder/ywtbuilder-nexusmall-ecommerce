package com.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.product.dto.PmsProductParam;
import com.mall.module.product.dto.PmsProductResult;
import com.mall.module.product.entity.*;
import com.mall.module.product.mapper.*;
import com.mall.module.product.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Set<String> HEAVY_TEXT_COLUMNS = Set.of(
            "description",
            "detail_desc",
            "detail_html",
            "detail_mobile_html",
            "album_pics"
    );

    private final PmsProductMapper productMapper;
    private final PmsMemberPriceMapper memberPriceMapper;
    private final PmsProductLadderMapper productLadderMapper;
    private final PmsProductFullReductionMapper productFullReductionMapper;
    private final PmsSkuStockMapper skuStockMapper;
    private final PmsProductAttributeValueMapper productAttributeValueMapper;

    public ProductServiceImpl(PmsProductMapper productMapper,
                              PmsMemberPriceMapper memberPriceMapper,
                              PmsProductLadderMapper productLadderMapper,
                              PmsProductFullReductionMapper productFullReductionMapper,
                              PmsSkuStockMapper skuStockMapper,
                              PmsProductAttributeValueMapper productAttributeValueMapper) {
        this.productMapper = productMapper;
        this.memberPriceMapper = memberPriceMapper;
        this.productLadderMapper = productLadderMapper;
        this.productFullReductionMapper = productFullReductionMapper;
        this.skuStockMapper = skuStockMapper;
        this.productAttributeValueMapper = productAttributeValueMapper;
    }

    @Override
    public Page<PmsProduct> list(String keyword, Long brandId, Long productCategoryId,
                                 Integer publishStatus, Integer verifyStatus,
                                 Integer pageNum, Integer pageSize) {
        Page<PmsProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProduct::getDeleteStatus, 0);
        if (publishStatus != null) {
            wrapper.eq(PmsProduct::getPublishStatus, publishStatus);
        }
        if (verifyStatus != null) {
            wrapper.eq(PmsProduct::getVerifyStatus, verifyStatus);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(PmsProduct::getName, keyword)
                    .or().like(PmsProduct::getProductSn, keyword));
        }
        if (brandId != null) {
            wrapper.eq(PmsProduct::getBrandId, brandId);
        }
        if (productCategoryId != null) {
            wrapper.eq(PmsProduct::getProductCategoryId, productCategoryId);
        }
        applyListProjection(wrapper);
        return productMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public int create(PmsProductParam productParam) {
        PmsProduct product = new PmsProduct();
        BeanUtils.copyProperties(productParam, product);
        product.setId(null);
        productMapper.insert(product);
        Long productId = product.getId();
        // 会员价格
        relateAndInsertList(memberPriceMapper, productParam.getMemberPriceList(), productId);
        // 阶梯价格
        relateAndInsertList(productLadderMapper, productParam.getProductLadderList(), productId);
        // 满减价格
        relateAndInsertList(productFullReductionMapper, productParam.getProductFullReductionList(), productId);
        // sku库存
        handleSkuStockCode(productParam.getSkuStockList(), productId);
        relateAndInsertList(skuStockMapper, productParam.getSkuStockList(), productId);
        // 属性值
        relateAndInsertList(productAttributeValueMapper, productParam.getProductAttributeValueList(), productId);
        return 1;
    }

    @Override
    @Transactional
    public int update(Long id, PmsProductParam productParam) {
        PmsProduct product = new PmsProduct();
        BeanUtils.copyProperties(productParam, product);
        product.setId(id);
        productMapper.updateById(product);
        // 会员价格
        memberPriceMapper.delete(new LambdaQueryWrapper<PmsMemberPrice>().eq(PmsMemberPrice::getProductId, id));
        relateAndInsertList(memberPriceMapper, productParam.getMemberPriceList(), id);
        // 阶梯价格
        productLadderMapper.delete(new LambdaQueryWrapper<PmsProductLadder>().eq(PmsProductLadder::getProductId, id));
        relateAndInsertList(productLadderMapper, productParam.getProductLadderList(), id);
        // 满减价格
        productFullReductionMapper.delete(new LambdaQueryWrapper<PmsProductFullReduction>().eq(PmsProductFullReduction::getProductId, id));
        relateAndInsertList(productFullReductionMapper, productParam.getProductFullReductionList(), id);
        // sku库存
        skuStockMapper.delete(new LambdaQueryWrapper<PmsSkuStock>().eq(PmsSkuStock::getProductId, id));
        handleSkuStockCode(productParam.getSkuStockList(), id);
        relateAndInsertList(skuStockMapper, productParam.getSkuStockList(), id);
        // 属性值
        productAttributeValueMapper.delete(new LambdaQueryWrapper<PmsProductAttributeValue>().eq(PmsProductAttributeValue::getProductId, id));
        relateAndInsertList(productAttributeValueMapper, productParam.getProductAttributeValueList(), id);
        return 1;
    }

    @Override
    public PmsProductResult getUpdateInfo(Long id) {
        PmsProduct product = productMapper.selectById(id);
        if (product == null) return null;
        PmsProductResult result = new PmsProductResult();
        BeanUtils.copyProperties(product, result);
        // 填充关联数据
        result.setMemberPriceList(memberPriceMapper.selectList(
                new LambdaQueryWrapper<PmsMemberPrice>().eq(PmsMemberPrice::getProductId, id)));
        result.setProductLadderList(productLadderMapper.selectList(
                new LambdaQueryWrapper<PmsProductLadder>().eq(PmsProductLadder::getProductId, id)));
        result.setProductFullReductionList(productFullReductionMapper.selectList(
                new LambdaQueryWrapper<PmsProductFullReduction>().eq(PmsProductFullReduction::getProductId, id)));
        result.setSkuStockList(skuStockMapper.selectList(
                new LambdaQueryWrapper<PmsSkuStock>().eq(PmsSkuStock::getProductId, id)));
        result.setProductAttributeValueList(productAttributeValueMapper.selectList(
                new LambdaQueryWrapper<PmsProductAttributeValue>().eq(PmsProductAttributeValue::getProductId, id)));
        // 设置父分类id
        if (product.getProductCategoryId() != null) {
            // 简化处理：根据需要可以注入 ProductCategoryMapper 查询
            result.setCateParentId(0L);
        }
        return result;
    }

    @Override
    public Page<PmsProduct> simpleList(String keyword, Integer pageNum, Integer pageSize) {
        Page<PmsProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProduct::getDeleteStatus, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(PmsProduct::getName, keyword)
                    .or().like(PmsProduct::getProductSn, keyword));
        }
        wrapper.select(PmsProduct::getId, PmsProduct::getName, PmsProduct::getProductSn,
                PmsProduct::getPrice, PmsProduct::getPic);
        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public int updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail) {
        PmsProduct record = new PmsProduct();
        record.setVerifyStatus(verifyStatus);
        return productMapper.update(record,
                new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids));
    }

    @Override
    public int updatePublishStatus(List<Long> ids, Integer publishStatus) {
        PmsProduct record = new PmsProduct();
        record.setPublishStatus(publishStatus);
        return productMapper.update(record,
                new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids));
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        PmsProduct record = new PmsProduct();
        record.setRecommandStatus(recommendStatus);
        return productMapper.update(record,
                new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids));
    }

    @Override
    public int updateNewStatus(List<Long> ids, Integer newStatus) {
        PmsProduct record = new PmsProduct();
        record.setNewStatus(newStatus);
        return productMapper.update(record,
                new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids));
    }

    @Override
    public int updateDeleteStatus(List<Long> ids, Integer deleteStatus) {
        PmsProduct record = new PmsProduct();
        record.setDeleteStatus(deleteStatus);
        return productMapper.update(record,
                new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids));
    }

    @Override
    public PmsProduct getById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<PmsProduct> listByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) return List.of();
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PmsProduct::getId, ids);
        applyListProjection(wrapper);
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<PmsProduct> listAll() {
        return productMapper.selectList(null);
    }

    private void applyListProjection(LambdaQueryWrapper<PmsProduct> wrapper) {
        wrapper.select(PmsProduct.class, info -> !HEAVY_TEXT_COLUMNS.contains(info.getColumn()));
    }

    /**
     * 生成sku编码
     */
    private void handleSkuStockCode(List<? extends PmsSkuStock> skuStockList, Long productId) {
        if (CollectionUtils.isEmpty(skuStockList)) return;
        for (int i = 0; i < skuStockList.size(); i++) {
            PmsSkuStock skuStock = skuStockList.get(i);
            if (!StringUtils.hasText(skuStock.getSkuCode())) {
                skuStock.setSkuCode(String.format("%s_%03d", String.valueOf(productId), i + 1));
            }
            skuStock.setProductId(productId);
        }
    }

    /**
     * 建立关系并批量插入
     */
    @SuppressWarnings("unchecked")
    private void relateAndInsertList(Object mapper, List<?> dataList, Long productId) {
        if (CollectionUtils.isEmpty(dataList)) return;
        for (Object item : dataList) {
            try {
                Method setProductId = item.getClass().getMethod("setProductId", Long.class);
                setProductId.invoke(item, productId);
            } catch (Exception e) {
                throw new RuntimeException("设置productId失败", e);
            }
        }
        for (Object item : dataList) {
            try {
                Method insert = mapper.getClass().getMethod("insert", Object.class);
                insert.invoke(mapper, item);
            } catch (Exception e) {
                throw new RuntimeException("批量插入失败", e);
            }
        }
    }
}
