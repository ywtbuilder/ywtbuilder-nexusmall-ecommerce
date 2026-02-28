package com.mall.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.order.dto.OmsMoneyInfoParam;
import com.mall.module.order.dto.OmsOrderDeliveryParam;
import com.mall.module.order.dto.OmsOrderDetail;
import com.mall.module.order.dto.OmsOrderQueryParam;
import com.mall.module.order.dto.OmsReceiverInfoParam;
import com.mall.module.order.entity.OmsOrder;
import com.mall.module.order.entity.OmsOrderItem;
import com.mall.module.order.entity.OmsOrderOperateHistory;
import com.mall.module.order.mapper.OmsOrderItemMapper;
import com.mall.module.order.mapper.OmsOrderMapper;
import com.mall.module.order.mapper.OmsOrderOperateHistoryMapper;
import com.mall.module.order.service.AdminOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final OmsOrderOperateHistoryMapper operateHistoryMapper;

    public AdminOrderServiceImpl(OmsOrderMapper orderMapper,
                                 OmsOrderItemMapper orderItemMapper,
                                 OmsOrderOperateHistoryMapper operateHistoryMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.operateHistoryMapper = operateHistoryMapper;
    }

    @Override
    public Page<OmsOrder> list(OmsOrderQueryParam queryParam, Integer pageNum, Integer pageSize) {
        Page<OmsOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryParam.getOrderSn())) {
            wrapper.eq(OmsOrder::getOrderSn, queryParam.getOrderSn());
        }
        if (queryParam.getStatus() != null) {
            wrapper.eq(OmsOrder::getStatus, queryParam.getStatus());
        }
        if (queryParam.getOrderType() != null) {
            wrapper.eq(OmsOrder::getOrderType, queryParam.getOrderType());
        }
        if (queryParam.getSourceType() != null) {
            wrapper.eq(OmsOrder::getSourceType, queryParam.getSourceType());
        }
        if (StringUtils.hasText(queryParam.getReceiverKeyword())) {
            wrapper.and(w -> w.like(OmsOrder::getReceiverName, queryParam.getReceiverKeyword())
                    .or().like(OmsOrder::getReceiverPhone, queryParam.getReceiverKeyword()));
        }
        wrapper.orderByDesc(OmsOrder::getCreateTime);
        return orderMapper.selectPage(page, wrapper);
    }

    @Override
    public OmsOrderDetail detail(Long id) {
        OmsOrder order = orderMapper.selectById(id);
        if (order == null) return null;
        OmsOrderDetail detail = new OmsOrderDetail();
        BeanUtils.copyProperties(order, detail);
        List<OmsOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, id));
        detail.setOrderItemList(items);
        return detail;
    }

    @Override
    @Transactional
    public int delivery(List<OmsOrderDeliveryParam> deliveryParams) {
        int count = 0;
        for (OmsOrderDeliveryParam param : deliveryParams) {
            OmsOrder order = new OmsOrder();
            order.setId(param.getOrderId());
            order.setDeliveryCompany(param.getDeliveryCompany());
            order.setDeliverySn(param.getDeliverySn());
            order.setDeliveryTime(LocalDateTime.now());
            order.setStatus(2); // 已发货
            orderMapper.updateById(order);
            // 记录操作历史
            insertOperateHistory(param.getOrderId(), 2, "发货", "后台管理员");
            count++;
        }
        return count;
    }

    @Override
    @Transactional
    public int close(List<Long> ids, String note) {
        OmsOrder order = new OmsOrder();
        order.setStatus(4); // 已关闭
        int count = orderMapper.update(order,
                new LambdaUpdateWrapper<OmsOrder>().in(OmsOrder::getId, ids));
        for (Long id : ids) {
            insertOperateHistory(id, 4, note, "后台管理员");
        }
        return count;
    }

    @Override
    public int delete(List<Long> ids) {
        OmsOrder order = new OmsOrder();
        order.setDeleteStatus(1);
        return orderMapper.update(order,
                new LambdaUpdateWrapper<OmsOrder>().in(OmsOrder::getId, ids));
    }

    @Override
    @Transactional
    public int updateReceiverInfo(OmsReceiverInfoParam param) {
        OmsOrder order = new OmsOrder();
        order.setId(param.getOrderId());
        order.setReceiverName(param.getReceiverName());
        order.setReceiverPhone(param.getReceiverPhone());
        order.setReceiverPostCode(param.getReceiverPostCode());
        order.setReceiverProvince(param.getReceiverProvince());
        order.setReceiverCity(param.getReceiverCity());
        order.setReceiverRegion(param.getReceiverRegion());
        order.setReceiverDetailAddress(param.getReceiverDetailAddress());
        order.setModifyTime(LocalDateTime.now());
        int count = orderMapper.updateById(order);
        insertOperateHistory(param.getOrderId(), param.getStatus(), "修改收货人信息", "后台管理员");
        return count;
    }

    @Override
    @Transactional
    public int updateMoneyInfo(OmsMoneyInfoParam param) {
        OmsOrder order = new OmsOrder();
        order.setId(param.getOrderId());
        order.setFreightAmount(param.getFreightAmount());
        order.setDiscountAmount(param.getDiscountAmount());
        order.setModifyTime(LocalDateTime.now());
        int count = orderMapper.updateById(order);
        insertOperateHistory(param.getOrderId(), param.getStatus(), "修改费用信息", "后台管理员");
        return count;
    }

    @Override
    @Transactional
    public int updateNote(Long id, String note, Integer status) {
        OmsOrder order = new OmsOrder();
        order.setId(id);
        order.setNote(note);
        order.setModifyTime(LocalDateTime.now());
        int count = orderMapper.updateById(order);
        insertOperateHistory(id, status, note, "后台管理员");
        return count;
    }

    private void insertOperateHistory(Long orderId, Integer orderStatus, String note, String operateMan) {
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(orderId);
        history.setOrderStatus(orderStatus);
        history.setNote(note);
        history.setOperateMan(operateMan);
        history.setCreateTime(LocalDateTime.now());
        operateHistoryMapper.insert(history);
    }
}
