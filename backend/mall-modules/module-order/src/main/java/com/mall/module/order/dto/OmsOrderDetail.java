package com.mall.module.order.dto;

import com.mall.module.order.entity.OmsOrder;
import com.mall.module.order.entity.OmsOrderItem;
import lombok.Data;

import java.util.List;

/**
 * 订单详情（包含订单商品）
 */
@Data
public class OmsOrderDetail extends OmsOrder {
    private List<OmsOrderItem> orderItemList;
}
