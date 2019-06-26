package com.fhb.customer.customer;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/26
 * Time: 15:44
 *
 * @author hbfang
 */


@ConfigurationProperties(prefix = "fhb")
public class HelloProperties {
    private static final String DEFAULT_NAME = "default name";
    private static final String DEFAULT_MSG = "default msg";
    private String name = DEFAULT_NAME;
    private String msg = DEFAULT_MSG;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
