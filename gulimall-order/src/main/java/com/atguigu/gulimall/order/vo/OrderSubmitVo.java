package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    private Long addrId;//地址id
    private Integer payType;//支付方式
    //无需提交需要购买的商品，去购物车获取即可
    //优惠 发票
    private String orderToken;//防重令牌
    private BigDecimal payPrice;//应付价格 可以验价
    private String note;//订单备注
    //用户相关信息,直接去session中取出即可
}
