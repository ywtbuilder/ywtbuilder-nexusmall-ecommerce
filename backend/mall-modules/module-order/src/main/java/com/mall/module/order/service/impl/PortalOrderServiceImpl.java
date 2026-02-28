package com.mall.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.order.dto.OmsOrderDetail;
import com.mall.module.order.entity.OmsOrder;
import com.mall.module.order.entity.OmsOrderItem;
import com.mall.module.order.entity.OmsOrderOperateHistory;
import com.mall.module.order.entity.OmsOrderSetting;
import com.mall.module.order.mapper.OmsOrderItemMapper;
import com.mall.module.order.mapper.OmsOrderMapper;
import com.mall.module.order.mapper.OmsOrderOperateHistoryMapper;
import com.mall.module.order.mapper.OmsOrderSettingMapper;
import com.mall.module.order.service.PortalOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PortalOrderServiceImpl implements PortalOrderService {

    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final OmsOrderOperateHistoryMapper operateHistoryMapper;
    private final OmsOrderSettingMapper orderSettingMapper;

    public PortalOrderServiceImpl(OmsOrderMapper orderMapper,
                                  OmsOrderItemMapper orderItemMapper,
                                  OmsOrderOperateHistoryMapper operateHistoryMapper,
                                  OmsOrderSettingMapper orderSettingMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.operateHistoryMapper = operateHistoryMapper;
        this.orderSettingMapper = orderSettingMapper;
    }

    @Override
    public Map<String, Object> generateConfirmOrder(List<Long> cartIds) {
        // 返回确认单信息（购物车商品、收货地址列表、可用优惠券等）
        // 实际业务需要跨模块调用 cart/member 服务，这里返回空Map作为骨架
        Map<String, Object> result = new HashMap<>();
        result.put("cartIds", cartIds);
        return result;
    }

    @Override
    @Transactional
    public OmsOrder generateOrder(Map<String, Object> orderParam, Long memberId) {
        OmsOrder order = new OmsOrder();
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(0); // 待付款
        order.setDeleteStatus(0);
        order.setConfirmStatus(0);
        order.setOrderSn(generateOrderSn());
        // 使用服务端校验的 memberId，忽略前端传入的 memberId
        order.setMemberId(memberId);
        if (orderParam.containsKey("receiverName")) {
            order.setReceiverName((String) orderParam.get("receiverName"));
        }
        if (orderParam.containsKey("receiverPhone")) {
            order.setReceiverPhone((String) orderParam.get("receiverPhone"));
        }
        if (orderParam.containsKey("receiverDetailAddress")) {
            order.setReceiverDetailAddress((String) orderParam.get("receiverDetailAddress"));
        }
        if (orderParam.containsKey("note")) {
            order.setNote((String) orderParam.get("note"));
        }
        if (orderParam.containsKey("payType") && orderParam.get("payType") instanceof Number payType) {
            order.setPayType(payType.intValue());
        }
        orderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional
    public int paySuccess(Long orderId, Integer payType) {
        OmsOrder order = new OmsOrder();
        order.setId(orderId);
        order.setStatus(1); // 待发货
        order.setPayType(payType);
        order.setPaymentTime(LocalDateTime.now());
        int count = orderMapper.updateById(order);
        // 记录操作历史
        insertOperateHistory(orderId, 1, "支付成功", "系统");
        return count;
    }

    @Override
    @Transactional
    public int cancelTimeOutOrder() {
        // 获取订单超时设置
        OmsOrderSetting setting = orderSettingMapper.selectById(1L);
        int overtime = setting != null ? setting.getNormalOrderOvertime() : 60;

        // 查询超时未支付订单
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(overtime);
        List<OmsOrder> timeoutOrders = orderMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getStatus, 0) // 待付款
                        .le(OmsOrder::getCreateTime, deadline));
        if (timeoutOrders.isEmpty()) return 0;

        // 批量取消
        int count = 0;
        for (OmsOrder order : timeoutOrders) {
            order.setStatus(4); // 已关闭
            orderMapper.updateById(order);
            insertOperateHistory(order.getId(), 4, "超时自动取消", "系统");
            count++;
        }
        return count;
    }

    @Override
    @Transactional
    public int cancelOrder(Long orderId) {
        // 系统级取消（超时/管理员），无所有权校验
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) {
            return 0;
        }
        order.setStatus(4);
        orderMapper.updateById(order);
        insertOperateHistory(orderId, 4, "系统取消订单", "系统");
        return 1;
    }

    @Override
    @Transactional
    public int cancelOrder(Long orderId, Long memberId) {
        // 用户级取消，校验订单归属
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getMemberId().equals(memberId)) {
            return 0; // 订单不存在或不属于当前用户
        }
        if (order.getStatus() != 0) {
            return 0; // 只有待付款订单可以取消
        }
        order.setStatus(4);
        orderMapper.updateById(order);
        insertOperateHistory(orderId, 4, "用户取消订单", "用户");
        return 1;
    }

    @Override
    public Page<OmsOrderDetail> list(Long memberId, Integer status, Integer pageNum, Integer pageSize) {
        Page<OmsOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrder::getMemberId, memberId);
        wrapper.eq(OmsOrder::getDeleteStatus, 0);
        if (status != null) {
            wrapper.eq(OmsOrder::getStatus, status);
        }
        wrapper.orderByDesc(OmsOrder::getCreateTime);
        Page<OmsOrder> orderPage = orderMapper.selectPage(page, wrapper);

        // 批量查订单项，避免 N+1
        List<OmsOrder> orders = orderPage.getRecords();
        List<OmsOrderDetail> detailList = new ArrayList<>();
        if (!orders.isEmpty()) {
            List<Long> orderIds = orders.stream().map(OmsOrder::getId).collect(Collectors.toList());
            List<OmsOrderItem> allItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OmsOrderItem>().in(OmsOrderItem::getOrderId, orderIds));
            Map<Long, List<OmsOrderItem>> itemMap = allItems.stream()
                    .collect(Collectors.groupingBy(OmsOrderItem::getOrderId));
            for (OmsOrder order : orders) {
                OmsOrderDetail detail = new OmsOrderDetail();
                BeanUtils.copyProperties(order, detail);
                detail.setOrderItemList(itemMap.getOrDefault(order.getId(), List.of()));
                detailList.add(detail);
            }
        }

        Page<OmsOrderDetail> detailPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        detailPage.setRecords(detailList);
        return detailPage;
    }

    @Override
    public OmsOrderDetail detail(Long orderId, Long memberId) {
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getMemberId().equals(memberId)) {
            return null; // 订单不存在或不属于当前用户
        }
        OmsOrderDetail detail = new OmsOrderDetail();
        BeanUtils.copyProperties(order, detail);
        List<OmsOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        detail.setOrderItemList(items);
        return detail;
    }

    @Override
    @Transactional
    public int confirmReceiveOrder(Long orderId, Long memberId) {
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getMemberId().equals(memberId)) {
            return 0; // 订单不存在或不属于当前用户
        }
        if (order.getStatus() != 2) {
            return 0; // 只有已发货订单可以确认收货
        }
        order.setStatus(3); // 已完成
        order.setConfirmStatus(1);
        order.setReceiveTime(LocalDateTime.now());
        orderMapper.updateById(order);
        insertOperateHistory(orderId, 3, "用户确认收货", "用户");
        return 1;
    }

    @Override
    @Transactional
    public int deleteOrder(Long orderId, Long memberId) {
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getMemberId().equals(memberId)) {
            return 0; // 订单不存在或不属于当前用户
        }
        order.setDeleteStatus(1);
        return orderMapper.updateById(order);
    }

    private String generateOrderSn() {
        // 简单的订单号生成：时间戳+随机数
        return System.currentTimeMillis() + String.valueOf((int) (Math.random() * 900 + 100));
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
