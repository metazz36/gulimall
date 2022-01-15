package com.atguigu.cart.vo;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class UserInfoTo {
    private Long userId;
    private String userkey;
    private boolean tempUser = false;//false为临时用户 true为非临时用户
}
