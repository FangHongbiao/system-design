package com.fhb.customer.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/26
 * Time: 16:13
 *
 * @author hbfang
 */

@SpringBootApplication
public class CustomerStarterApplication {
    @Autowired
    HelloService helloService;

    public static void main(String[] args) {
        SpringApplication.run(CustomerStarterApplication.class, args);
    }
}
