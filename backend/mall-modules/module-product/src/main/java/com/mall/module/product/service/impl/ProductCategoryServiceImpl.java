package com.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.product.dto.PmsProductCategoryParam;
import com.mall.module.product.dto.PmsProductCategoryWithChildrenItem;
import com.mall.module.product.entity.PmsProductCategory;
import com.mall.module.product.entity.PmsProductCategoryAttributeRelation;
import com.mall.module.product.mapper.PmsProductCategoryAttributeRelationMapper;
import com.mall.module.product.mapper.PmsProductCategoryMapper;
import com.mall.module.product.service.ProductCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final PmsProductCategoryMapper categoryMapper;
    private final PmsProductCategoryAttributeRelationMapper categoryAttributeRelationMapper;

    public ProductCategoryServiceImpl(PmsProductCategoryMapper categoryMapper,
                                      PmsProductCategoryAttributeRelationMapper categoryAttributeRelationMapper) {
        this.categoryMapper = categoryMapper;
        this.categoryAttributeRelationMapper = categoryAttributeRelationMapper;
    }

    @Override
    public List<PmsProductCategoryWithChildrenItem> listWithChildren() {
        // 查询所有一级分类
        List<PmsProductCategory> topCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<PmsProductCategory>()
                        .eq(PmsProductCategory::getParentId, 0)
                        .orderByDesc(PmsProductCategory::getSort));
        // 查询所有二级分类
        List<PmsProductCategory> allChildren = categoryMapper.selectList(
                new LambdaQueryWrapper<PmsProductCategory>()
                        .ne(PmsProductCategory::getParentId, 0)
                        .orderByDesc(PmsProductCategory::getSort));
        // 组装
        List<PmsProductCategoryWithChildrenItem> result = new ArrayList<>();
        for (PmsProductCategory top : topCategories) {
            PmsProductCategoryWithChildrenItem item = new PmsProductCategoryWithChildrenItem();
            BeanUtils.copyProperties(top, item);
            List<PmsProductCategory> children = allChildren.stream()
                    .filter(c -> c.getParentId().equals(top.getId()))
                    .collect(Collectors.toList());
            item.setChildren(children);
            result.add(item);
        }
        return result;
    }

    @Override
    public Page<PmsProductCategory> list(Long parentId, Integer pageNum, Integer pageSize) {
        Page<PmsProductCategory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmsProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProductCategory::getParentId, parentId)
                .orderByDesc(PmsProductCategory::getSort);
        return categoryMapper.selectPage(page, wrapper);
    }

    @Override
    public PmsProductCategory getItem(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    @Transactional
    public int create(PmsProductCategoryParam param) {
        PmsProductCategory category = new PmsProductCategory();
        BeanUtils.copyProperties(param, category);
        category.setProductCount(0);
        category.setLevel(param.getParentId() == 0 ? 0 : 1);
        categoryMapper.insert(category);
        // 创建筛选属性关联
        setCategoryAttributeRelation(category.getId(), param.getProductAttributeIdList());
        return 1;
    }

    @Override
    @Transactional
    public int update(Long id, PmsProductCategoryParam param) {
        PmsProductCategory category = new PmsProductCategory();
        BeanUtils.copyProperties(param, category);
        category.setId(id);
        categoryMapper.updateById(category);
        // 更新筛选属性关联
        categoryAttributeRelationMapper.delete(
                new LambdaQueryWrapper<PmsProductCategoryAttributeRelation>()
                        .eq(PmsProductCategoryAttributeRelation::getProductCategoryId, id));
        setCategoryAttributeRelation(id, param.getProductAttributeIdList());
        return 1;
    }

    @Override
    public int delete(Long id) {
        return categoryMapper.deleteById(id);
    }

    @Override
    public int updateNavStatus(List<Long> ids, Integer navStatus) {
        PmsProductCategory category = new PmsProductCategory();
        category.setNavStatus(navStatus);
        return categoryMapper.update(category,
                new LambdaUpdateWrapper<PmsProductCategory>().in(PmsProductCategory::getId, ids));
    }

    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        PmsProductCategory category = new PmsProductCategory();
        category.setShowStatus(showStatus);
        return categoryMapper.update(category,
                new LambdaUpdateWrapper<PmsProductCategory>().in(PmsProductCategory::getId, ids));
    }

    private void setCategoryAttributeRelation(Long categoryId, List<Long> attributeIdList) {
        if (CollectionUtils.isEmpty(attributeIdList)) return;
        for (Long attrId : attributeIdList) {
            PmsProductCategoryAttributeRelation relation = new PmsProductCategoryAttributeRelation();
            relation.setProductCategoryId(categoryId);
            relation.setProductAttributeId(attrId);
            categoryAttributeRelationMapper.insert(relation);
        }
    }
}
