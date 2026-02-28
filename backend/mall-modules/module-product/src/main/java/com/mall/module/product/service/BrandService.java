package com.mall.module.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.product.entity.PmsBrand;

import java.util.List;

/**
 * 商品品牌服务
 */
public interface BrandService {

    /** 获取全部品牌 */
    List<PmsBrand> listAll();

    /** 创建品牌 */
    int create(PmsBrand brand);

    /** 更新品牌 */
    int update(Long id, PmsBrand brand);

    /** 删除品牌 */
    int delete(Long id);

    /** 分页查询品牌 */
    Page<PmsBrand> list(String keyword, int pageNum, int pageSize);

    /** 获取品牌详情 */
    PmsBrand getItem(Long id);

    /** 批量删除 */
    int deleteBatch(List<Long> ids);

    /** 批量更新显示状态 */
    int updateShowStatus(List<Long> ids, Integer showStatus);

    /** 批量更新厂家制造商状态 */
    int updateFactoryStatus(List<Long> ids, Integer factoryStatus);
}
