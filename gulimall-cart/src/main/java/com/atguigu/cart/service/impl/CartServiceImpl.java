package com.atguigu.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.cart.feign.ProductFeignService;
import com.atguigu.cart.interceptor.CartInterceptor;
import com.atguigu.cart.service.CartService;
import com.atguigu.cart.vo.Cart;
import com.atguigu.cart.vo.CartItem;
import com.atguigu.cart.vo.SkuInfoVo;
import com.atguigu.cart.vo.UserInfoTo;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CartServiceImpl implements CartService{

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    private final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            //购物车无此商品 添加新商品到购物车
            CartItem cartItem = new CartItem();
            //远程查询当前要添加商品的信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(data.getSkuId());
                cartItem.setPrice(data.getPrice());
            }, executor);

            //远程查询sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrValuesTask = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValuesTask).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);

            return cartItem;
        } else {
            //购物车有此商品，修改数量即可
            CartItem cartItem1 = JSON.parseObject(res, CartItem.class);
            cartItem1.setCount(cartItem1.getCount() + num);

            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem1));
            return cartItem1;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String)cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(res, CartItem.class);
        return cartItem;
    }

    private List<CartItem> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if(values != null && values.size() > 0){
            List<CartItem> collect = values.stream().map(obj -> {
                String str = (String)obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }


    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {

        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() != null){
            //1、登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //临时购物车的键
            String temptCartKey = CART_PREFIX + userInfoTo.getUserkey();

            //2、如果临时购物车的数据还未进行合并
            List<CartItem> tempCartItems = getCartItems(temptCartKey);
            if (tempCartItems != null) {
                //临时购物车有数据需要进行合并操作
                for (CartItem item : tempCartItems) {
                    addToCart(item.getSkuId(),item.getCount());
                }
                //清除临时购物车的数据
                clearCart(temptCartKey);
            }

            //3、获取登录后的购物车数据【包含合并过来的临时购物车的数据和登录后购物车的数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }else{
            //2、没登录
            String cartKey = CART_PREFIX + userInfoTo.getUserkey();
            //获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void clearCart(String cartkey) {
        redisTemplate.delete(cartkey);
    }

    @Override
    public void checkItem(Long skuId, Integer checked) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(checked == 1?true:false);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);

    }

    @Override
    public void deleteItem(Long deleteId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(deleteId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() == null){
            return null;
        }else{
            String cartKey = CART_PREFIX + userInfoTo.getUserId();//用户标识
            List<CartItem> cartItems = getCartItems(cartKey);
            List<CartItem> collect = cartItems.stream().filter(item -> item.getCheck())
                    .map(item->{
                        //更新为最新价格
                        R price = productFeignService.getPrice(item.getSkuId());
                        String data =(String) price.get("data");
                        item.setPrice(new BigDecimal(data));
                        return item;
                    }).collect(Collectors.toList());
            return collect;
        }

    }


    /**
     * 获取到我们要操作的购物车
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //1、
        String cartKey="";
        if(userInfoTo.getUserId() != null){
            //gulimall:cart:1
            cartKey = CART_PREFIX + userInfoTo.getUserId();//用户标识
        }else{
            cartKey = CART_PREFIX +userInfoTo.getUserkey();//临时用户标识
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }
}
