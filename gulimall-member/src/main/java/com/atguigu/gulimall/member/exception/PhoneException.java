package com.atguigu.gulimall.member.exception;

public class PhoneException extends  RuntimeException{

    public PhoneException(){
        super("手机号存在");
    }
}
