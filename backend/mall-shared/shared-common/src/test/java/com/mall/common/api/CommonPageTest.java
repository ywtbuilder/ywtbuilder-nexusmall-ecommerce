package com.mall.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonPage 单元测试
 */
class CommonPageTest {

    @Test
    void from_shouldMapPageFields() {
        Page<String> page = new Page<>(2, 10);
        page.setTotal(35);
        page.setRecords(List.of("a", "b", "c"));

        CommonPage<String> result = CommonPage.from(page);
        assertEquals(2, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(35, result.getTotal());
        assertEquals(4, result.getTotalPage()); // 35 / 10 = 3.5, ceil = 4
        assertEquals(3, result.getList().size());
    }

    @Test
    void of_shouldBuildManually() {
        CommonPage<Integer> result = CommonPage.of(List.of(1, 2, 3), 1, 10, 100);
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(100, result.getTotal());
        assertEquals(10, result.getTotalPage()); // 100 / 10 = 10
        assertEquals(3, result.getList().size());
    }

    @Test
    void totalPage_shouldCeilCorrectly() {
        CommonPage<String> result = CommonPage.of(List.of(), 1, 10, 0);
        assertEquals(0, result.getTotalPage());

        CommonPage<String> result2 = CommonPage.of(List.of(), 1, 10, 1);
        assertEquals(1, result2.getTotalPage());
    }
}
