package com.mall.module.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.order.dto.OmsMoneyInfoParam;
import com.mall.module.order.dto.OmsOrderDeliveryParam;
import com.mall.module.order.dto.OmsOrderDetail;
import com.mall.module.order.dto.OmsOrderQueryParam;
import com.mall.module.order.dto.OmsReceiverInfoParam;
import com.mall.module.order.entity.OmsOrder;

import java.util.List;

/**
 * 后台订单管理服务
 */
public interface AdminOrderService {

    /** 分页查询订单 */
    Page<OmsOrder> list(OmsOrderQueryParam queryParam, Integer pageNum, Integer pageSize);

    /** 获取订单详情 */
    OmsOrderDetail detail(Long id);

    /** 批量发货 */
    int delivery(List<OmsOrderDeliveryParam> deliveryParams);

    /** 批量关闭订单 */
    int close(List<Long> ids, String note);

    /** 批量删除订单 */
    int delete(List<Long> ids);

    /** 修改收货人信息 */
    int updateReceiverInfo(OmsReceiverInfoParam receiverInfoParam);

    /** 修改订单费用信息 */
    int updateMoneyInfo(OmsMoneyInfoParam moneyInfoParam);

    /** 备注订单 */
    int updateNote(Long id, String note, Integer status);
}
