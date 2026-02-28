package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片资源 BLOB 存储（按 SHA-256 hash 去重）
 */
@Data
@TableName("pms_asset")
public class PmsAsset {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** SHA-256 hex, 用于去重和 ETag */
    private String imageHash;
    /** MIME 类型, 如 image/jpeg */
    private String mimeType;
    /** 图片宽度 px */
    private Integer width;
    /** 图片高度 px */
    private Integer height;
    /** 文件字节数 */
    private Integer fileSize;
    /** 原始文件名 */
    private String originalFilename;
    /** 图片二进制本体 */
    private byte[] imageData;
    private LocalDateTime createdAt;
}
