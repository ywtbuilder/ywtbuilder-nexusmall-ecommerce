package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品内容归档（原始 JSON/HTML 备份 + 评论概要）
 */
@Data
@TableName("pms_product_content")
public class PmsProductContent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    /** 纯文本描述/卖点 */
    private String descriptionText;
    /** 原始规格 JSON */
    private String specRawJson;
    /** 原始 SKU JSON */
    private String skuRawJson;
    /** 评论概要 */
    private String commentSummary;
    /** 原始 API 响应完整 JSON（回溯审计） */
    private String rawApiJson;
    /** 导出包文件结构摘要 */
    private String fileStructure;
    /** 原始导出包路径 */
    private String sourceDir;
    private LocalDateTime importTime;
}
