package com.mall.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.order.entity.OmsOrderReturnApply;
import com.mall.module.order.mapper.OmsOrderReturnApplyMapper;
import com.mall.module.order.service.OrderReturnApplyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderReturnApplyServiceImpl implements OrderReturnApplyService {

    private final OmsOrderReturnApplyMapper returnApplyMapper;

    public OrderReturnApplyServiceImpl(OmsOrderReturnApplyMapper returnApplyMapper) {
        this.returnApplyMapper = returnApplyMapper;
    }

    @Override
    public int create(OmsOrderReturnApply returnApply) {
        returnApply.setCreateTime(LocalDateTime.now());
        returnApply.setStatus(0); // 待处理
        return returnApplyMapper.insert(returnApply);
    }

    @Override
    public Page<OmsOrderReturnApply> list(Long id, Integer status, String createTime,
                                           String handleMan, String handleTime,
                                           Integer pageNum, Integer pageSize) {
        Page<OmsOrderReturnApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OmsOrderReturnApply> wrapper = new LambdaQueryWrapper<>();
        if (id != null) {
            wrapper.eq(OmsOrderReturnApply::getId, id);
        }
        if (status != null) {
            wrapper.eq(OmsOrderReturnApply::getStatus, status);
        }
        if (StringUtils.hasText(handleMan)) {
            wrapper.like(OmsOrderReturnApply::getHandleMan, handleMan);
        }
        wrapper.orderByDesc(OmsOrderReturnApply::getCreateTime);
        return returnApplyMapper.selectPage(page, wrapper);
    }

    @Override
    public OmsOrderReturnApply detail(Long id) {
        return returnApplyMapper.selectById(id);
    }

    @Override
    public int updateStatus(Long id, Integer status, String handleNote, String handleMan,
                            Long companyAddressId) {
        OmsOrderReturnApply apply = new OmsOrderReturnApply();
        apply.setId(id);
        apply.setStatus(status);
        apply.setHandleNote(handleNote);
        apply.setHandleMan(handleMan);
        apply.setCompanyAddressId(companyAddressId);
        apply.setHandleTime(LocalDateTime.now());
        return returnApplyMapper.updateById(apply);
    }

    @Override
    public int delete(List<Long> ids) {
        return returnApplyMapper.deleteByIds(ids);
    }
}
