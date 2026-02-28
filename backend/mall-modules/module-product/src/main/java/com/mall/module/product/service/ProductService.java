package com.mall.module.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.product.dto.PmsProductParam;
import com.mall.module.product.dto.PmsProductResult;
import com.mall.module.product.entity.PmsProduct;

import java.util.List;

/**
 * 商品管理服务
 */
public interface ProductService {

    /** 分页查询商品 */
    Page<PmsProduct> list(String keyword, Long brandId, Long productCategoryId,
                          Integer publishStatus, Integer verifyStatus, Integer pageNum, Integer pageSize);

    /** 创建商品 */
    int create(PmsProductParam productParam);

    /** 更新商品 */
    int update(Long id, PmsProductParam productParam);

    /** 获取商品编辑信息 */
    PmsProductResult getUpdateInfo(Long id);

    /** 根据商品名称/货号模糊查询 */
    Page<PmsProduct> simpleList(String keyword, Integer pageNum, Integer pageSize);

    /** 批量修改审核状态 */
    int updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail);

    /** 批量上下架 */
    int updatePublishStatus(List<Long> ids, Integer publishStatus);

    /** 批量推荐 */
    int updateRecommendStatus(List<Long> ids, Integer recommendStatus);

    /** 批量设为新品 */
    int updateNewStatus(List<Long> ids, Integer newStatus);

    /** 批量修改删除状态 */
    int updateDeleteStatus(List<Long> ids, Integer deleteStatus);

    /** 根据id获取商品 */
    PmsProduct getById(Long id);

    /** 根据ID列表批量查询商品 */
    List<PmsProduct> listByIds(List<Long> ids);

    /** 查询所有商品（供 ES 全量导入使用） */
    List<PmsProduct> listAll();
}
