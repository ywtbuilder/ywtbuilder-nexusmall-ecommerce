package com.mall.module.member.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 会员商品收藏（MongoDB）
 */
@Data
@Document("member_product_collection")
@CompoundIndex(name = "idx_member_product", def = "{'memberId': 1, 'productId': 1}", unique = true)
public class MemberProductCollection {
    @Id
    private String id;
    private Long memberId;
    private String memberNickname;
    private String memberIcon;
    private Long productId;
    private String productName;
    private String productPic;
    private String productSubTitle;
    private String productPrice;
    private LocalDateTime createTime;
}
