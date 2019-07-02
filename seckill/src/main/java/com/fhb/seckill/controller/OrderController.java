package com.fhb.seckill.controller;

import com.fhb.seckill.domain.MiaoshaUser;
import com.fhb.seckill.domain.OrderInfo;
import com.fhb.seckill.redis.RedisService;
import com.fhb.seckill.result.CodeMsg;
import com.fhb.seckill.result.Result;
import com.fhb.seckill.service.GoodsService;
import com.fhb.seckill.service.MiaoshaUserService;
import com.fhb.seckill.service.OrderService;
import com.fhb.seckill.vo.GoodsVo;
import com.fhb.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
    MiaoshaUserService userService;
	
	@Autowired
    RedisService redisService;

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;
	
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, MiaoshaUser user, @RequestParam("orderId") long orderId) {

        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        OrderInfo order = orderService.getOrderById(orderId);

        if (order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }

        long goodsId = order.getGoodsId();


        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);


        OrderDetailVo vo = new OrderDetailVo();

        vo.setGoods(goods);
        vo.setOrder(order);
        return Result.success(vo);
    }
    
}
