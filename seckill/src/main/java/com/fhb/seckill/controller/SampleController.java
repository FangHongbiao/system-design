package com.fhb.seckill.controller;

import com.fhb.seckill.domain.User;
import com.fhb.seckill.rabbitmq.MQSender;
import com.fhb.seckill.redis.RedisService;
import com.fhb.seckill.redis.UserKey;
import com.fhb.seckill.result.CodeMsg;
import com.fhb.seckill.result.Result;
import com.fhb.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/demo")
public class SampleController {

	@Autowired
    UserService userService;
	
	@Autowired
    RedisService redisService;

	@Autowired
    MQSender mqSender;

    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> mqHeader() {

        mqSender.sendHeader("hello rabbitmq header");
        return Result.success("hello rabbitmq header");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> mqFanout() {

        mqSender.sendFanout("hello rabbitmq fanout");
        return Result.success("hello rabbitmq fanout");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> mqTppic() {

        mqSender.sendTopic("hello rabbitmq topic");
        return Result.success("hello rabbitmq topic");
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {

        mqSender.send("hello rabbitmq");
        return Result.success("hello rabbitmq");
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> home() {
        return Result.success("Hello，world");
    }
    
    @RequestMapping("/error")
    @ResponseBody
    public Result<String> error() {
        return Result.error(CodeMsg.SESSION_ERROR);
    }
    
    @RequestMapping("/hello/themaleaf")
    public String themaleaf(Model model) {
        model.addAttribute("name", "Joshua");
        return "hello";
    }
    
    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {
    	User user = userService.getById(1);
        return Result.success(user);
    }
    
    
    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
    	userService.tx();
        return Result.success(true);
    }
    
    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
    	User  user  = redisService.get(UserKey.getById, ""+1, User.class);
        return Result.success(user);
    }
    
    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
    	User user  = new User();
    	user.setId(1);
    	user.setName("1111");
    	redisService.set(UserKey.getById, ""+1, user);
        return Result.success(true);
    }
    
    
}
