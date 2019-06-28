package com.fhb.sso.web.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/28
 * Time: 21:39
 *
 * @author hbfang
 */

@Controller
public class WebController {

    @RequestMapping(value = {"", "index"}, method = RequestMethod.GET)
    public String index(){
        return "index";
    }
}
