package com.family.myfamily;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@MapperScan("com.family.myfamily.mapper")
@ComponentScan(basePackages = "com.family.myfamily")
public class MyFamilyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyFamilyApplication.class, args);
    }
}
