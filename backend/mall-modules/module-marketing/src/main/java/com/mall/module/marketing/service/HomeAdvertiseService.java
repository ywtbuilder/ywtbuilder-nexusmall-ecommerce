package com.mall.module.marketing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.marketing.entity.SmsHomeAdvertise;

import java.util.List;

/**
 * 首页广告服务
 */
public interface HomeAdvertiseService {
    Page<SmsHomeAdvertise> list(String name, Integer type, String endTime,
                                Integer pageNum, Integer pageSize);
    int create(SmsHomeAdvertise advertise);
    int update(Long id, SmsHomeAdvertise advertise);
    int delete(List<Long> ids);
    SmsHomeAdvertise getItem(Long id);
    int updateStatus(Long id, Integer status);
}
