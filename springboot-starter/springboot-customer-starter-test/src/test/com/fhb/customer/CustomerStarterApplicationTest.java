package com.fhb.customer;

import com.fhb.customer.customer.CustomerStarterApplication;
import com.fhb.customer.customer.HelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/26
 * Time: 16:47
 *
 * @author hbfang
 */


@RunWith(SpringRunner.class)
@SpringBootTest(classes = CustomerStarterApplication.class)
public class CustomerStarterApplicationTest {

    @Autowired
    HelloService helloService;
    @Test
    public void contextLoads() {
        System.out.println(helloService.sayHello());
    }
}
