package com.xlaoy.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by Administrator on 2018/2/1 0001.
 */
@SpringBootApplication(scanBasePackages = {"com.xlaoy"})
public class WebApplaction {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(WebApplaction.class);
        springApplication.run(args);
    }

}
