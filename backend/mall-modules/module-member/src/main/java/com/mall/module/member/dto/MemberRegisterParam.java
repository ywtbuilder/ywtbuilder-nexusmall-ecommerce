package com.mall.module.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 会员注册参数
 */
@Data
public class MemberRegisterParam {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 手机号 */
    private String telephone;

    /** 验证码 */
    private String authCode;
}
