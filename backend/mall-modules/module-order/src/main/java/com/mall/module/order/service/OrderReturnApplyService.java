package com.mall.module.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.order.entity.OmsOrderReturnApply;

import java.util.List;

/**
 * 退货申请服务
 */
public interface OrderReturnApplyService {

    /** App 端创建退货申请 */
    int create(OmsOrderReturnApply returnApply);

    /** Admin 端分页查询 */
    Page<OmsOrderReturnApply> list(Long id, Integer status, String createTime,
                                    String handleMan, String handleTime,
                                    Integer pageNum, Integer pageSize);

    /** Admin 端获取详情 */
    OmsOrderReturnApply detail(Long id);

    /** Admin 端更新状态 */
    int updateStatus(Long id, Integer status, String handleNote, String handleMan,
                     Long companyAddressId);

    /** Admin 端批量删除 */
    int delete(List<Long> ids);
}
