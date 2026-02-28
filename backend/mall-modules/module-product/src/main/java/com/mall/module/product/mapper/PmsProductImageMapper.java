package com.mall.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.module.product.entity.PmsProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmsProductImageMapper extends BaseMapper<PmsProductImage> {
    @Select("""
            SELECT CONCAT('/api/asset/image/', a.image_hash)
            FROM pms_product_image pi
            JOIN pms_asset a ON a.id = pi.asset_id
            WHERE pi.product_id = #{productId}
              AND pi.image_type = #{imageType}
            ORDER BY pi.sort_order ASC, pi.id ASC
            """)
    List<String> selectImageUrlsByProductIdAndType(@Param("productId") Long productId,
                                                   @Param("imageType") Integer imageType);
}
