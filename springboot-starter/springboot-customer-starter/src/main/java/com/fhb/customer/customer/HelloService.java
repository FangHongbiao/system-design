package com.fhb.customer.customer;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/26
 * Time: 15:47
 *
 * @author hbfang
 */


public class HelloService {
    private String msg;
    private String name;
    public String sayHello() {
        return name + " say " + msg + " !";
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
