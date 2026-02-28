package com.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.product.entity.PmsBrand;
import com.mall.module.product.mapper.PmsBrandMapper;
import com.mall.module.product.service.BrandService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    private final PmsBrandMapper brandMapper;

    public BrandServiceImpl(PmsBrandMapper brandMapper) {
        this.brandMapper = brandMapper;
    }

    @Override
    public List<PmsBrand> listAll() {
        return brandMapper.selectList(null);
    }

    @Override
    public int create(PmsBrand brand) {
        brand.setId(null);
        return brandMapper.insert(brand);
    }

    @Override
    public int update(Long id, PmsBrand brand) {
        brand.setId(id);
        return brandMapper.updateById(brand);
    }

    @Override
    public int delete(Long id) {
        return brandMapper.deleteById(id);
    }

    @Override
    public Page<PmsBrand> list(String keyword, int pageNum, int pageSize) {
        Page<PmsBrand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmsBrand> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(PmsBrand::getName, keyword);
        }
        return brandMapper.selectPage(page, wrapper);
    }

    @Override
    public PmsBrand getItem(Long id) {
        return brandMapper.selectById(id);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        return brandMapper.deleteByIds(ids);
    }

    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        PmsBrand brand = new PmsBrand();
        brand.setShowStatus(showStatus);
        return brandMapper.update(brand,
                new LambdaUpdateWrapper<PmsBrand>().in(PmsBrand::getId, ids));
    }

    @Override
    public int updateFactoryStatus(List<Long> ids, Integer factoryStatus) {
        PmsBrand brand = new PmsBrand();
        brand.setFactoryStatus(factoryStatus);
        return brandMapper.update(brand,
                new LambdaUpdateWrapper<PmsBrand>().in(PmsBrand::getId, ids));
    }
}
