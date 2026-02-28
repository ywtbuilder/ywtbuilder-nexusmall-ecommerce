package com.mall.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页数据封装
 */
@Data
@NoArgsConstructor
public class CommonPage<T> {
    private long pageNum;
    private long pageSize;
    private long total;
    private long totalPage;
    private List<T> list;

    /**
     * 从 MyBatis-Plus Page 对象转换
     */
    public static <T> CommonPage<T> from(Page<T> page) {
        CommonPage<T> result = new CommonPage<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setTotalPage(page.getPages());
        result.setList(page.getRecords());
        return result;
    }

    /**
     * 手动构建分页
     */
    public static <T> CommonPage<T> of(List<T> list, long pageNum, long pageSize, long total) {
        CommonPage<T> result = new CommonPage<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setTotalPage((total + pageSize - 1) / pageSize);
        result.setList(list);
        return result;
    }
}
