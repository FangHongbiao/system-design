package com.fhb.seckill.controller;

import com.fhb.seckill.domain.MiaoshaOrder;
import com.fhb.seckill.domain.MiaoshaUser;
import com.fhb.seckill.domain.OrderInfo;
import com.fhb.seckill.redis.RedisService;
import com.fhb.seckill.result.CodeMsg;
import com.fhb.seckill.result.Result;
import com.fhb.seckill.service.GoodsService;
import com.fhb.seckill.service.MiaoshaService;
import com.fhb.seckill.service.MiaoshaUserService;
import com.fhb.seckill.service.OrderService;
import com.fhb.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	/**
	 * QPS:1306
	 * 5000 * 10
	 * */
    @RequestMapping(value = "/do_miaosha", method = RequestMethod.POST)
	@ResponseBody
    public Result<OrderInfo> list(Model model, MiaoshaUser user,
					   @RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
		System.out.println(user);

		if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}

		System.out.println(user);
		System.out.println(orderService);
		//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);

        return Result.success(orderInfo);
    }
}
