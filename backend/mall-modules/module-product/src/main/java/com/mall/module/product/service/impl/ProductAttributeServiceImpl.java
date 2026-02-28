package com.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.product.entity.PmsProductAttribute;
import com.mall.module.product.entity.PmsProductCategoryAttributeRelation;
import com.mall.module.product.mapper.PmsProductAttributeMapper;
import com.mall.module.product.mapper.PmsProductCategoryAttributeRelationMapper;
import com.mall.module.product.service.ProductAttributeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductAttributeServiceImpl implements ProductAttributeService {

    private final PmsProductAttributeMapper attributeMapper;
    private final PmsProductCategoryAttributeRelationMapper categoryAttributeRelationMapper;

    public ProductAttributeServiceImpl(PmsProductAttributeMapper attributeMapper,
                                       PmsProductCategoryAttributeRelationMapper categoryAttributeRelationMapper) {
        this.attributeMapper = attributeMapper;
        this.categoryAttributeRelationMapper = categoryAttributeRelationMapper;
    }

    @Override
    public Page<PmsProductAttribute> list(Long cid, Integer type, Integer pageNum, Integer pageSize) {
        Page<PmsProductAttribute> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmsProductAttribute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProductAttribute::getProductAttributeCategoryId, cid);
        if (type != null) {
            wrapper.eq(PmsProductAttribute::getType, type);
        }
        wrapper.orderByDesc(PmsProductAttribute::getSort);
        return attributeMapper.selectPage(page, wrapper);
    }

    @Override
    public PmsProductAttribute getItem(Long id) {
        return attributeMapper.selectById(id);
    }

    @Override
    public int create(PmsProductAttribute attr) {
        attr.setId(null);
        return attributeMapper.insert(attr);
    }

    @Override
    public int update(Long id, PmsProductAttribute attr) {
        attr.setId(id);
        return attributeMapper.updateById(attr);
    }

    @Override
    public int delete(List<Long> ids) {
        return attributeMapper.deleteByIds(ids);
    }

    @Override
    public List<PmsProductAttribute> getProductAttrInfo(Long productCategoryId) {
        List<PmsProductCategoryAttributeRelation> relations = categoryAttributeRelationMapper.selectList(
                new LambdaQueryWrapper<PmsProductCategoryAttributeRelation>()
                        .eq(PmsProductCategoryAttributeRelation::getProductCategoryId, productCategoryId));
        List<Long> attrIds = relations.stream()
                .map(PmsProductCategoryAttributeRelation::getProductAttributeId)
                .collect(Collectors.toList());
        if (attrIds.isEmpty()) return List.of();
        return attributeMapper.selectByIds(attrIds);
    }
}
