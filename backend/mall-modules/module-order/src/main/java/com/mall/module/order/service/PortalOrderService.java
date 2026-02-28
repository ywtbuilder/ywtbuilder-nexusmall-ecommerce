package com.mall.module.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.order.dto.OmsOrderDetail;
import com.mall.module.order.entity.OmsOrder;

import java.util.List;
import java.util.Map;

/**
 * 前台订单服务
 */
public interface PortalOrderService {

    /** 根据购物车信息生成确认单（保留接口，确认页数据由 BFF 层聚合） */
    Map<String, Object> generateConfirmOrder(List<Long> cartIds);

    /** 根据下单参数生成订单，使用服务端校验的 memberId（禁止信任前端传入） */
    OmsOrder generateOrder(Map<String, Object> orderParam, Long memberId);

    /** 用户支付成功回调 */
    int paySuccess(Long orderId, Integer payType);

    /** 自动取消超时订单 */
    int cancelTimeOutOrder();

    /** 系统取消订单（超时/管理员，无所有权校验） */
    int cancelOrder(Long orderId);

    /** 用户取消订单（含所有权校验） */
    int cancelOrder(Long orderId, Long memberId);

    /** 按状态分页获取当前会员的订单列表（按 memberId 隔离，含订单项） */
    Page<OmsOrderDetail> list(Long memberId, Integer status, Integer pageNum, Integer pageSize);

    /** 获取订单详情（含所有权校验） */
    OmsOrderDetail detail(Long orderId, Long memberId);

    /** 用户确认收货（含所有权校验） */
    int confirmReceiveOrder(Long orderId, Long memberId);

    /** 用户删除订单（含所有权校验） */
    int deleteOrder(Long orderId, Long memberId);
}
