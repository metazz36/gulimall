package com.atguigu.gulimall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/*
* 如何使用nacos作为配置中心统一管理配置
* 1、引入依赖
*   <dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    </dependency>
* 2、创建bootstrap.properties
*    spring.application.name=gulimall-coupon
     spring.cloud.nacos.config.server-addr=127.0.0.1:8848
* 3、给配置中心默认添加一个数据集（Data Id）
*    gulimall-coupons.properties 默认规则：应用名.properties
* 4、给应用名.properties添加配置
* 5、动态获取配置
*    在controller类上添加注解
*    @RefreshScope：动态获取并刷新配置
*    @Value("${配置项的名}"):：获取对应配置项名的值
* 6、如果配置中心和当前应用的配置文件中都配置了相同的项，优先使用配置中心的配置（即配置中心优先级大于application.properties）
*
*
* 细节
* 1、命名空间
* 2、配置集
* 3、配置集ID
* 4、配置分组个配置集
*
* */

@EnableDiscoveryClient
@SpringBootApplication
public class GulimallCouponApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallCouponApplication.class, args);
	}

}
