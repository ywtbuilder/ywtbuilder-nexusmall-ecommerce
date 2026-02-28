package com.mall.module.search.repository;

import com.mall.module.search.entity.EsProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * ES 商品 Repository
 */
public interface EsProductRepository extends ElasticsearchRepository<EsProduct, Long> {

    Page<EsProduct> findByNameContainingOrSubTitleContainingOrKeywordsContaining(
            String name, String subTitle, String keywords, Pageable pageable);
}
