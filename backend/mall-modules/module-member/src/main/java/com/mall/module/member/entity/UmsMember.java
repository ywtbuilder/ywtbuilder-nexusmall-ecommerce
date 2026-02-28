package com.mall.module.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员表 ums_member
 */
@Data
@TableName("ums_member")
public class UmsMember implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberLevelId;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private String icon;
    private String avatarUrl;
    private Integer gender;
    private LocalDate birthday;
    private String city;
    private String job;
    private String personalizedSignature;
    private Integer sourceType;
    private Integer integration;
    private Integer growth;
    private Integer luckeyCount;
    private Integer historyIntegration;
}
