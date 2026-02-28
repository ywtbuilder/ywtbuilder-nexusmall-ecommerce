package com.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oms_order_operate_history")
public class OmsOrderOperateHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    /** 操作人：用户/系统/后台管理员 */
    private String operateMan;
    private LocalDateTime createTime;
    /** 订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单 */
    private Integer orderStatus;
    /** 备注 */
    private String note;
}
