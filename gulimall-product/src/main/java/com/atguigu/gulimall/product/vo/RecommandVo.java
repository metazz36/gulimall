package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecommandVo {

    private Long id;

    private Long memberId;

    private Long spuId;

    private String spuName;

    private String spuPic;

    private Long categoryId;

    private Long skuId;

    private String skuName;

    private String skuPic;

    private BigDecimal skuPrice;

}
