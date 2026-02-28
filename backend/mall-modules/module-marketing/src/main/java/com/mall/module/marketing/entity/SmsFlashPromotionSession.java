package com.mall.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_flash_promotion_session")
public class SmsFlashPromotionSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 场次名称 */
    private String name;
    /** 每日开始时间 */
    private LocalDateTime startTime;
    /** 每日结束时间 */
    private LocalDateTime endTime;
    /** 启用状态：0->不启用；1->启用 */
    private Integer status;
    private LocalDateTime createTime;
}
