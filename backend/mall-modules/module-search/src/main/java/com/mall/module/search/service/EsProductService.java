package com.mall.module.search.service;

import com.mall.module.search.entity.EsProduct;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * ES 商品搜索服务
 */
public interface EsProductService {

    /**
     * 从数据库导入所有商品到 ES
     */
    int importAll();

    /**
     * 根据 ID 删除商品
     */
    void delete(Long id);

    /**
     * 根据 ID 批量删除
     */
    void delete(List<Long> ids);

    /**
     * 根据 ID 创建或更新
     */
    void create(Long id);

    /**
     * 综合搜索
     *
     * @param keyword            关键词
     * @param brandId            品牌 ID（可选）
     * @param productCategoryId  分类 ID（可选）
     * @param pageNum            页码
     * @param pageSize           每页大小
     * @param sort               排序字段 (0-综合 1-新品 2-销量 3-价格)
     */
    Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId,
                           Integer pageNum, Integer pageSize, Integer sort);
}
