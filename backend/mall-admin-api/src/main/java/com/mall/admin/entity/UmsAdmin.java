package com.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台用户
 */
@Data
@TableName("ums_admin")
public class UmsAdmin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String icon;
    private String email;
    private String nickName;
    private String note;
    private LocalDateTime createTime;
    private LocalDateTime loginTime;
    /** 0->禁用；1->启用 */
    private Integer status;
}
