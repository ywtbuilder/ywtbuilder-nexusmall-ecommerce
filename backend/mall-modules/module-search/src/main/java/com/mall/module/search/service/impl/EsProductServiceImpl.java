package com.mall.module.search.service.impl;

import com.mall.module.search.entity.EsProduct;
import com.mall.module.search.repository.EsProductRepository;
import com.mall.module.search.service.EsProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EsProductServiceImpl implements EsProductService {

    private static final Logger log = LoggerFactory.getLogger(EsProductServiceImpl.class);

    private final EsProductRepository esProductRepository;

    public EsProductServiceImpl(EsProductRepository esProductRepository) {
        this.esProductRepository = esProductRepository;
    }

    @Override
    public int importAll() {
        // 实际需要注入 ProductMapper 从 MySQL 读取商品
        // 跨模块调用在 BFF 层或通过事件同步完成
        log.info("[EsProduct] importAll — 需要通过 BFF 层调用，从 MySQL 拉取商品数据后写入 ES");
        return 0;
    }

    @Override
    public void delete(Long id) {
        esProductRepository.deleteById(id);
    }

    @Override
    public void delete(List<Long> ids) {
        esProductRepository.deleteAllById(ids);
    }

    @Override
    public void create(Long id) {
        // 实际需要从 MySQL 读取商品信息转为 EsProduct 后保存
        log.info("[EsProduct] create/update — id={}, 需要通过 BFF 层调用", id);
    }

    /**
     * 批量保存 ES 文档（供 BFF 层调用）
     */
    public void saveAll(List<EsProduct> products) {
        esProductRepository.saveAll(products);
        log.info("[EsProduct] 批量索引 {} 条商品", products.size());
    }

    /**
     * 保存单个 ES 文档（供 BFF 层调用）
     */
    public void save(EsProduct product) {
        esProductRepository.save(product);
    }

    @Override
    public Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId,
                                   Integer pageNum, Integer pageSize, Integer sort) {
        Pageable pageable = buildPageable(pageNum, pageSize, sort);

        if (StringUtils.hasText(keyword)) {
            return esProductRepository.findByNameContainingOrSubTitleContainingOrKeywordsContaining(
                    keyword, keyword, keyword, pageable);
        }
        // 无关键词时返回全部（分页）
        return esProductRepository.findAll(pageable);
    }

    private Pageable buildPageable(Integer pageNum, Integer pageSize, Integer sort) {
        Sort sortObj;
        if (sort == null || sort == 0) {
            sortObj = Sort.by(Sort.Direction.DESC, "id");
        } else if (sort == 1) {
            sortObj = Sort.by(Sort.Direction.DESC, "newStatus");
        } else if (sort == 2) {
            sortObj = Sort.by(Sort.Direction.DESC, "sale");
        } else if (sort == 3) {
            sortObj = Sort.by(Sort.Direction.ASC, "price");
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "id");
        }
        return PageRequest.of(pageNum - 1, pageSize, sortObj);
    }
}
