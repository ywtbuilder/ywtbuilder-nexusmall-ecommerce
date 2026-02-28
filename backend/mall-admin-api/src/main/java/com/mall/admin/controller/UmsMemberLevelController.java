package com.mall.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.CommonResult;
import com.mall.module.member.entity.UmsMemberLevel;
import com.mall.module.member.mapper.UmsMemberLevelMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员等级管理 — 对标 V1 UmsMemberLevelController（V2 缺失）
 */
@Tag(name = "UmsMemberLevel", description = "会员等级管理")
@RestController
@RequestMapping("/memberLevel")
public class UmsMemberLevelController {

    private final UmsMemberLevelMapper umsMemberLevelMapper;

    public UmsMemberLevelController(UmsMemberLevelMapper umsMemberLevelMapper) {
        this.umsMemberLevelMapper = umsMemberLevelMapper;
    }

    @Operation(summary = "获取所有会员等级")
    @GetMapping("/list")
    public CommonResult<List<UmsMemberLevel>> list(@RequestParam(defaultValue = "0") Integer defaultStatus) {
        LambdaQueryWrapper<UmsMemberLevel> wrapper = new LambdaQueryWrapper<>();
        if (defaultStatus != null && defaultStatus == 1) {
            wrapper.eq(UmsMemberLevel::getDefaultStatus, 1);
        }
        List<UmsMemberLevel> list = umsMemberLevelMapper.selectList(wrapper);
        return CommonResult.success(list);
    }
}
