package com.mall.module.member.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 会员品牌关注（MongoDB）
 */
@Data
@Document("member_brand_attention")
@CompoundIndex(name = "idx_member_brand", def = "{'memberId': 1, 'brandId': 1}", unique = true)
public class MemberBrandAttention {
    @Id
    private String id;
    private Long memberId;
    private String memberNickname;
    private String memberIcon;
    private Long brandId;
    private String brandName;
    private String brandLogo;
    private String brandCity;
    private LocalDateTime createTime;
}
