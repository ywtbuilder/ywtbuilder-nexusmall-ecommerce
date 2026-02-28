package com.mall.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.UmsResource;

import java.util.List;

/**
 * 资源管理 Service
 */
public interface UmsResourceService {

    int create(UmsResource resource);

    int update(Long id, UmsResource resource);

    int delete(Long id);

    /** 分页列表 */
    Page<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword,
                           Integer pageNum, Integer pageSize);

    /** 获取全部资源 */
    List<UmsResource> listAll();

    /** 根据ID获取 */
    UmsResource getItem(Long id);
}
