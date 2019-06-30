package com.fhb.sso.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/28
 * Time: 21:33
 *
 * @author hbfang
 */

@SpringBootApplication(scanBasePackages = "com.fhb.sso.web")
public class WebApplication{
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
