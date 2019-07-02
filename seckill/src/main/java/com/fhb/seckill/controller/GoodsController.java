package com.fhb.seckill.controller;

import java.util.List;

import com.alibaba.druid.util.StringUtils;
import com.fhb.seckill.domain.MiaoshaUser;
import com.fhb.seckill.redis.GoodsKey;
import com.fhb.seckill.redis.RedisService;
import com.fhb.seckill.result.Result;
import com.fhb.seckill.service.GoodsService;
import com.fhb.seckill.service.MiaoshaUserService;
import com.fhb.seckill.vo.GoodsDetailVo;
import com.fhb.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;
    /**
     * QPS:1267
     * 5000 * 10
     */
    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user) {

        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);

        // 取缓存
        if (!StringUtils.isEmpty(html)) {
            return html;
        }


        model.addAttribute("user", user);
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);

        // 手动渲染模板
        SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap(), applicationContext);

        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);

        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
    }

    @RequestMapping(value = "/to_detail/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
                         @PathVariable("goodsId") long goodsId) {

        String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId, String.class);

        // 取缓存
        if (!StringUtils.isEmpty(html)) {
            return html;
        }


        model.addAttribute("user", user);

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;
        //秒杀还没开始，倒计时
        if (now < startAt) {
            miaoshaStatus = 0;
            remainSeconds = (int) ((startAt - now) / 1000);
        //秒杀已经结束
        } else if (now > endAt) {
            miaoshaStatus = 2;
            remainSeconds = -1;
        } else {//秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);


        // 手动渲染模板
        SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap(), applicationContext);

        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);

        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
        }
        return html;
    }


    /**
     * 页面静态化
     * @param request
     * @param response
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail1(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
                                         @PathVariable("goodsId") long goodsId) {

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);


        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;

        //秒杀还没开始，倒计时
        if (now < startAt) {
            miaoshaStatus = 0;
            remainSeconds = (int) ((startAt - now) / 1000);
            //秒杀已经结束
        } else if (now > endAt) {
            miaoshaStatus = 2;
            remainSeconds = -1;
        } else {//秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }

        GoodsDetailVo vo = new GoodsDetailVo();

        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(miaoshaStatus);
        return Result.success(vo);
    }

}
