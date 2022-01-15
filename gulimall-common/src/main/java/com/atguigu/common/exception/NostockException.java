package com.atguigu.common.exception;

public class NostockException extends RuntimeException {
    private Long skuId;
    private String msg;
    public NostockException(Long skuId){
        super("商品id:" + skuId +"没有足够的库存了");
    }

    public NostockException(String msg){
        super(msg);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
