package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author : SuperYan
 * @Version : 1.0
 * @Date : 2019/9/22 11:53
 */
@MapperScan("com.leyou.mapper")
@SpringBootApplication
@EnableDiscoveryClient
public class ItemServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItemServiceApplication.class);
    }
}
