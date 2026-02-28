package com.mall.module.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.marketing.entity.SmsHomeAdvertise;
import com.mall.module.marketing.mapper.SmsHomeAdvertiseMapper;
import com.mall.module.marketing.service.HomeAdvertiseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class HomeAdvertiseServiceImpl implements HomeAdvertiseService {

    private final SmsHomeAdvertiseMapper advertiseMapper;

    public HomeAdvertiseServiceImpl(SmsHomeAdvertiseMapper advertiseMapper) {
        this.advertiseMapper = advertiseMapper;
    }

    @Override
    public Page<SmsHomeAdvertise> list(String name, Integer type, String endTime,
                                        Integer pageNum, Integer pageSize) {
        Page<SmsHomeAdvertise> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsHomeAdvertise> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(SmsHomeAdvertise::getName, name);
        }
        if (type != null) {
            wrapper.eq(SmsHomeAdvertise::getType, type);
        }
        wrapper.orderByDesc(SmsHomeAdvertise::getSort);
        return advertiseMapper.selectPage(page, wrapper);
    }

    @Override
    public int create(SmsHomeAdvertise advertise) {
        advertise.setClickCount(0);
        advertise.setOrderCount(0);
        return advertiseMapper.insert(advertise);
    }

    @Override
    public int update(Long id, SmsHomeAdvertise advertise) {
        advertise.setId(id);
        return advertiseMapper.updateById(advertise);
    }

    @Override
    public int delete(List<Long> ids) {
        return advertiseMapper.deleteByIds(ids);
    }

    @Override
    public SmsHomeAdvertise getItem(Long id) {
        return advertiseMapper.selectById(id);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        SmsHomeAdvertise ad = new SmsHomeAdvertise();
        ad.setId(id);
        ad.setStatus(status);
        return advertiseMapper.updateById(ad);
    }
}
