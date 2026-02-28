package com.mall.module.member.service;

import com.mall.module.member.dto.MemberRegisterParam;
import com.mall.module.member.entity.UmsMember;

/**
 * 会员注册 / 认证服务
 */
public interface MemberService {

    void register(MemberRegisterParam param);

    String login(String username, String password);

    String getAuthCode(String telephone);

    void updatePassword(String telephone, String password, String authCode);

    UmsMember getMemberByUsername(String username);

    UmsMember getMemberById(Long id);

    String refreshToken(String oldToken);
}
