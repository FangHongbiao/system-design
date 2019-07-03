package com.fhb.seckill.controller;

import com.fhb.seckill.domain.MiaoshaOrder;
import com.fhb.seckill.domain.MiaoshaUser;
import com.fhb.seckill.rabbitmq.MQSender;
import com.fhb.seckill.rabbitmq.MiaoshaMessage;
import com.fhb.seckill.redis.GoodsKey;
import com.fhb.seckill.redis.MiaoshaKey;
import com.fhb.seckill.redis.RedisService;
import com.fhb.seckill.result.CodeMsg;
import com.fhb.seckill.result.Result;
import com.fhb.seckill.service.GoodsService;
import com.fhb.seckill.service.MiaoshaService;
import com.fhb.seckill.service.MiaoshaUserService;
import com.fhb.seckill.service.OrderService;
import com.fhb.seckill.util.MD5Util;
import com.fhb.seckill.util.UUIDUtil;
import com.fhb.seckill.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

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

    @Autowired
    MQSender sender;


    private Map<Long, Boolean> localOverMap = new HashMap<>();

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user, @RequestParam("goodsId") long goodsId) {

        model.addAttribute("user", user);
        System.out.println(user);

        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);

        try {
            ServletOutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAO_SHA_FAIL);
        }

    }

    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(Model model, MiaoshaUser user, @RequestParam("goodsId") long goodsId, @RequestParam("verifyCode") int verfifyCode) {

        model.addAttribute("user", user);
        System.out.println(user);

        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        String path = miaoshaService.createMiaoshaPath(user, goodsId);

        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verfifyCode);

        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        return Result.success(path);
    }


    /**
     * orderId: 成功
     * -1 : 失败
     * 0 : 排队
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> getMiaoshaResult(Model model, MiaoshaUser user,
                                         @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        System.out.println(user);

        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);

        return Result.success(result);
    }

    /**
     * 0 : 排队中
     * <p>
     * QPS:1306
     * 5000 * 10
     */
    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
                                   @RequestParam("goodsId") long goodsId, @PathVariable("path") String path) {
        model.addAttribute("user", user);
        System.out.println(user);

        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);

        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        // 内存标记, 减少Redis访问
        Boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        // 预减库存
        Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        // 入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);

    	/*
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
        */
        return Result.success(0);
    }

    /**
     * 系统初始化
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();

        if (goodsList == null) {
            return;
        }

        for (GoodsVo goodsVo : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goodsVo.getId(), goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(), false);
        }
    }
}
