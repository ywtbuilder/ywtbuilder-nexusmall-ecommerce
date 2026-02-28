package com.mall.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.CommonPage;
import com.mall.common.api.CommonResult;
import com.mall.module.marketing.entity.SmsHomeRecommendSubject;
import com.mall.module.marketing.mapper.SmsHomeRecommendSubjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页专题推荐管理 — 对标 V1 SmsHomeRecommendSubjectController（V2 缺失）
 */
@Tag(name = "SmsHomeRecommendSubject", description = "首页专题推荐")
@RestController
@RequestMapping("/home/recommendSubject")
public class SmsHomeRecommendSubjectController {

    private final SmsHomeRecommendSubjectMapper smsHomeRecommendSubjectMapper;

    public SmsHomeRecommendSubjectController(SmsHomeRecommendSubjectMapper smsHomeRecommendSubjectMapper) {
        this.smsHomeRecommendSubjectMapper = smsHomeRecommendSubjectMapper;
    }

    @Operation(summary = "创建专题推荐")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody List<SmsHomeRecommendSubject> subjectList) {
        int count = 0;
        for (SmsHomeRecommendSubject item : subjectList) {
            smsHomeRecommendSubjectMapper.insert(item);
            count++;
        }
        return CommonResult.success(count);
    }

    @Operation(summary = "修改推荐排序")
    @PostMapping("/update/sort/{id}")
    public CommonResult<Integer> updateSort(@PathVariable Long id, @RequestParam Integer sort) {
        SmsHomeRecommendSubject subject = new SmsHomeRecommendSubject();
        subject.setId(id);
        subject.setSort(sort);
        int count = smsHomeRecommendSubjectMapper.updateById(subject);
        return CommonResult.success(count);
    }

    @Operation(summary = "删除专题推荐")
    @PostMapping("/delete")
    public CommonResult<Integer> delete(@RequestParam List<Long> ids) {
        int count = smsHomeRecommendSubjectMapper.deleteBatchIds(ids);
        return CommonResult.success(count);
    }

    @Operation(summary = "专题推荐分页列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsHomeRecommendSubject>> list(@RequestParam(required = false) String subjectName,
                                                                  @RequestParam(required = false) Integer recommendStatus,
                                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                                  @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<SmsHomeRecommendSubject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsHomeRecommendSubject> wrapper = new LambdaQueryWrapper<>();
        if (subjectName != null) {
            wrapper.like(SmsHomeRecommendSubject::getSubjectName, subjectName);
        }
        if (recommendStatus != null) {
            wrapper.eq(SmsHomeRecommendSubject::getRecommendStatus, recommendStatus);
        }
        smsHomeRecommendSubjectMapper.selectPage(page, wrapper);
        return CommonResult.success(CommonPage.from(page));
    }

    @Operation(summary = "批量修改推荐状态")
    @PostMapping("/update/recommendStatus")
    public CommonResult<Integer> updateRecommendStatus(@RequestParam List<Long> ids,
                                                        @RequestParam Integer recommendStatus) {
        int count = 0;
        for (Long id : ids) {
            SmsHomeRecommendSubject subject = new SmsHomeRecommendSubject();
            subject.setId(id);
            subject.setRecommendStatus(recommendStatus);
            smsHomeRecommendSubjectMapper.updateById(subject);
            count++;
        }
        return CommonResult.success(count);
    }
}
