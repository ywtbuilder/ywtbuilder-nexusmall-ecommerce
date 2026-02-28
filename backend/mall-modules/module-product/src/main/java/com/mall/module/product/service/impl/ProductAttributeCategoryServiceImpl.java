package com.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.product.dto.PmsProductAttributeCategoryItem;
import com.mall.module.product.entity.PmsProductAttribute;
import com.mall.module.product.entity.PmsProductAttributeCategory;
import com.mall.module.product.mapper.PmsProductAttributeCategoryMapper;
import com.mall.module.product.mapper.PmsProductAttributeMapper;
import com.mall.module.product.service.ProductAttributeCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductAttributeCategoryServiceImpl implements ProductAttributeCategoryService {

    private final PmsProductAttributeCategoryMapper categoryMapper;
    private final PmsProductAttributeMapper attributeMapper;

    public ProductAttributeCategoryServiceImpl(PmsProductAttributeCategoryMapper categoryMapper,
                                               PmsProductAttributeMapper attributeMapper) {
        this.categoryMapper = categoryMapper;
        this.attributeMapper = attributeMapper;
    }

    @Override
    public List<PmsProductAttributeCategoryItem> getListWithAttr() {
        List<PmsProductAttributeCategory> categories = categoryMapper.selectList(null);
        List<PmsProductAttributeCategoryItem> result = new ArrayList<>();
        for (PmsProductAttributeCategory cat : categories) {
            PmsProductAttributeCategoryItem item = new PmsProductAttributeCategoryItem();
            BeanUtils.copyProperties(cat, item);
            List<PmsProductAttribute> attrs = attributeMapper.selectList(
                    new LambdaQueryWrapper<PmsProductAttribute>()
                            .eq(PmsProductAttribute::getProductAttributeCategoryId, cat.getId()));
            item.setProductAttributeList(attrs);
            result.add(item);
        }
        return result;
    }

    @Override
    public Page<PmsProductAttributeCategory> list(Integer pageNum, Integer pageSize) {
        return categoryMapper.selectPage(new Page<>(pageNum, pageSize), null);
    }

    @Override
    public int create(String name) {
        PmsProductAttributeCategory cat = new PmsProductAttributeCategory();
        cat.setName(name);
        cat.setAttributeCount(0);
        cat.setParamCount(0);
        return categoryMapper.insert(cat);
    }

    @Override
    public int update(Long id, String name) {
        PmsProductAttributeCategory cat = new PmsProductAttributeCategory();
        cat.setId(id);
        cat.setName(name);
        return categoryMapper.updateById(cat);
    }

    @Override
    public int delete(Long id) {
        return categoryMapper.deleteById(id);
    }
}
