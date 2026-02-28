package com.mall.module.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员等级表 ums_member_level
 */
@Data
@TableName("ums_member_level")
public class UmsMemberLevel implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer growthPoint;
    private Integer defaultStatus;
    private Integer freeFreightPoint;
    private Integer commentGrowthPoint;
    private Integer priviledgeFreeFreight;
    private Integer priviledgeSignIn;
    private Integer priviledgeComment;
    private Integer priviledgePromotion;
    private Integer priviledgeMemberPrice;
    private Integer priviledgeBirthday;
    private String note;
}
