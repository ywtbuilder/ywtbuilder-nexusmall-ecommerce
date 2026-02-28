package com.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oms_order_return_reason")
public class OmsOrderReturnReason {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 退货原因名称 */
    private String name;
    private Integer sort;
    /** 状态：0->不启用；1->启用 */
    private Integer status;
    private LocalDateTime createTime;
}
