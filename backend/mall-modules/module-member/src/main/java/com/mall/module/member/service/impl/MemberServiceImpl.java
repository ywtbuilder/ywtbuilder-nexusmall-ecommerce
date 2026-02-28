package com.mall.module.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.Asserts;
import com.mall.common.service.AuthCodeService;
import com.mall.module.member.dto.MemberRegisterParam;
import com.mall.module.member.entity.UmsMember;
import com.mall.module.member.entity.UmsMemberLevel;
import com.mall.module.member.mapper.UmsMemberLevelMapper;
import com.mall.module.member.mapper.UmsMemberMapper;
import com.mall.module.member.service.MemberService;
import com.mall.security.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MemberServiceImpl implements MemberService {

    private final UmsMemberMapper memberMapper;
    private final UmsMemberLevelMapper memberLevelMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthCodeService authCodeService;

    public MemberServiceImpl(UmsMemberMapper memberMapper,
                             UmsMemberLevelMapper memberLevelMapper,
                             JwtService jwtService,
                             PasswordEncoder passwordEncoder,
                             AuthCodeService authCodeService) {
        this.memberMapper = memberMapper;
        this.memberLevelMapper = memberLevelMapper;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authCodeService = authCodeService;
    }

    @Override
    public void register(MemberRegisterParam param) {
        // 检查用户名是否已存在
        UmsMember existing = getMemberByUsername(param.getUsername());
        if (existing != null) {
            Asserts.fail("该用户名已被注册");
        }
        // 验证验证码
        if (param.getTelephone() != null && param.getAuthCode() != null) {
            if (!authCodeService.verifyAuthCode(param.getTelephone(), param.getAuthCode())) {
                Asserts.fail("验证码不正确或已过期");
            }
        }
        UmsMember member = new UmsMember();
        member.setUsername(param.getUsername());
        member.setPassword(passwordEncoder.encode(param.getPassword()));
        member.setPhone(param.getTelephone());
        member.setNickname(param.getUsername());
        member.setStatus(1);
        member.setCreateTime(LocalDateTime.now());
        member.setIntegration(0);
        member.setGrowth(0);
        member.setHistoryIntegration(0);
        member.setLuckeyCount(0);
        // 设置默认会员等级
        UmsMemberLevel defaultLevel = memberLevelMapper.selectOne(
                new LambdaQueryWrapper<UmsMemberLevel>().eq(UmsMemberLevel::getDefaultStatus, 1));
        if (defaultLevel != null) {
            member.setMemberLevelId(defaultLevel.getId());
        }
        memberMapper.insert(member);
    }

    @Override
    public String login(String username, String password) {
        UmsMember member = getMemberByUsername(username);
        if (member == null) {
            Asserts.fail("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, member.getPassword())) {
            Asserts.fail("用户名或密码错误");
        }
        if (member.getStatus() != 1) {
            Asserts.fail("该账号已被禁用");
        }
        return jwtService.generateToken(member.getUsername());
    }

    @Override
    public String getAuthCode(String telephone) {
        return authCodeService.generateAuthCode(telephone);
    }

    @Override
    public void updatePassword(String telephone, String password, String authCode) {
        if (!authCodeService.verifyAuthCode(telephone, authCode)) {
            Asserts.fail("验证码不正确或已过期");
        }
        UmsMember member = memberMapper.selectOne(
                new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getPhone, telephone));
        if (member == null) {
            Asserts.fail("该手机号未注册");
        }
        member.setPassword(passwordEncoder.encode(password));
        memberMapper.updateById(member);
    }

    @Override
    public UmsMember getMemberByUsername(String username) {
        return memberMapper.selectOne(
                new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getUsername, username));
    }

    @Override
    public UmsMember getMemberById(Long id) {
        return memberMapper.selectById(id);
    }

    @Override
    public String refreshToken(String oldToken) {
        return jwtService.refreshToken(oldToken);
    }
}
