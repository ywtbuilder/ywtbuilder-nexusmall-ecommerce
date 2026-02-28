package com.mall.admin.controller;

import com.mall.common.api.CommonResult;
import com.mall.module.order.entity.OmsCompanyAddress;
import com.mall.module.order.mapper.OmsCompanyAddressMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公司收发货地址管理 — 对标 V1 OmsCompanyAddressController（V2 缺失）
 */
@Tag(name = "OmsCompanyAddress", description = "公司收发货地址")
@RestController
@RequestMapping("/companyAddress")
public class OmsCompanyAddressController {

    private final OmsCompanyAddressMapper omsCompanyAddressMapper;

    public OmsCompanyAddressController(OmsCompanyAddressMapper omsCompanyAddressMapper) {
        this.omsCompanyAddressMapper = omsCompanyAddressMapper;
    }

    @Operation(summary = "获取所有公司收发货地址")
    @GetMapping("/list")
    public CommonResult<List<OmsCompanyAddress>> list() {
        return CommonResult.success(omsCompanyAddressMapper.selectList(null));
    }
}
