package com.atguigu.gulimall.gulimallauthserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.gulimallauthserver.feign.MemberFeignService;
import com.atguigu.gulimall.gulimallauthserver.feign.ThirdPartFeignService;
import com.atguigu.gulimall.gulimallauthserver.vo.UserLoginVo;
import com.atguigu.gulimall.gulimallauthserver.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;


    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){
        //1、接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            //活动存入redis的时间，用当前时间减去存入redis的时间，判断用户手机号是否在60s内发送验证码
            long currentTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - currentTime < 60000) {
                //60s内不能再发
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        //2、验证码的再次效验 redis.存key-phone,value-code
        String code = UUID.randomUUID().toString().substring(0, 5);
        String subString = code + "_" + System.currentTimeMillis();

        //redis缓存验证码,防止同一个手机号在60秒内再次发送验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,subString,10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone,code);
        return R.ok();
    }


    /**
     *
     * TODO: 重定向携带数据：利用session原理，将数据放在session中。
     * TODO:只要跳转到下一个页面取出这个数据以后，session里面的数据就会删掉
     * TODO：分布式下的session问题
     * RedirectAttributes：重定向也可以保留数据，不会丢失
     * 用户注册
     * @return
     */
    @PostMapping("/regist")
    public String register(@Valid UserRegistVo vo, BindingResult result,Model model,
                           RedirectAttributes attributes){
        //如果有错误回到注册页面
        if (result.hasErrors()) {

            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
//            model.addAttribute("errors",errors);
            attributes.addFlashAttribute("errors",errors);
            //效验出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //1、校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if(!StringUtils.isEmpty(s)){
            if (code.equals(s.split("_")[0])) {
                //删除验证码;令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //验证码通过。真正注册 调用远程服务进行注册
                R r = memberFeignService.regist(vo);
                if(r.getCode() == 0){
                    //注册成功回到登录页
                    return "redirect:http://auth.gulimall.com/login.html";
                }else{
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg",new TypeReference<String>(){}));
                    attributes.addFlashAttribute("errors",errors);
                    return  "redirect:http://auth.gulimall.com/reg.html";
                }

            }else{
                Map<String, String> errors = new HashMap<>();
                errors.put("errors","验证码错误");
                attributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else{

            Map<String, String> errors = new HashMap<>();
            errors.put("errors","验证码错误");
            attributes.addFlashAttribute("errors",errors);
            //效验出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";

        }

    }


    @GetMapping(value = "/login.html")
    public String loginPage(HttpSession session) {

        //从session先取出来用户的信息，判断用户是否已经登录过了
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        //如果用户没登录那就跳转到登录页面
        if (attribute == null) {
            return "login";
        } else {
            return "redirect:http://gulimall.com";
        }

    }

    @PostMapping("/login")
        public String login(UserLoginVo vo,RedirectAttributes redirectAttributes,HttpSession session){

        //远程登录
        R r = memberFeignService.login(vo);
        if(r.getCode() == 0){
            //成功放到session中
            MemberResponseVo data = r.getData("data", new TypeReference<MemberResponseVo>() {});
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:http://gulimall.com";
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";

        }
    }
}
