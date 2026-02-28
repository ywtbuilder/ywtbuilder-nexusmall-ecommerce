package com.mall.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_flash_promotion")
public class SmsFlashPromotion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    /** 开始日期 */
    private LocalDateTime startDate;
    /** 结束日期 */
    private LocalDateTime endDate;
    /** 上下线状态 */
    private Integer status;
    private LocalDateTime createTime;
}
