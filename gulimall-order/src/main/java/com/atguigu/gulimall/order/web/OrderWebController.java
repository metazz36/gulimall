package com.atguigu.gulimall.order.web;

import com.atguigu.common.exception.NostockException;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import com.sun.media.sound.MidiOutDeviceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;


    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest request) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo =  orderService.confirmOrder();

        model.addAttribute("orderConfirmData",orderConfirmVo);
        //展示订单确认的数据
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){

        try{
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

            System.out.println("订单提交的数据"+vo);
            if(responseVo.getCode() == 0){
                //下单成功来到支付选择页
                model.addAttribute("submitOrderResp",responseVo);
                return "pay";
            }else{
                String msg = "下单失败";
                switch(responseVo.getCode()){
                    case 1: msg += "订单信息过期，请刷新再次提交";break;
                    case 2: msg += "订单商品价格发生变化，请确认后再次提交";break;
                    case 3: msg += "库存锁定失败,商品库存不足";break;

                }
                redirectAttributes.addFlashAttribute("msg",msg);

                //下单失败回到订单确认页重新确认订单信息
                return "redirect:http://order.gulimall.com/toTrade";
            }
        }catch (NostockException e){
            //下单失败回到订单确认页重新确认订单信息
            String msg = e.getMessage();
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }


    }
}
