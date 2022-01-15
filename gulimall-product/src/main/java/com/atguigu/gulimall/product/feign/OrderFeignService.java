package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.vo.OrderItemEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;



@FeignClient("gulimall-order")
public interface OrderFeignService {

    @RequestMapping("/order/orderitem/listall")
    public R listall();


}
