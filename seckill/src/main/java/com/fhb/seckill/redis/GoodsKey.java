package com.fhb.seckill.redis;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/7/2
 * Time: 8:51
 *
 * @author hbfang
 */

public class GoodsKey  extends  BasePrefix{




    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static GoodsKey getGoodsList = new GoodsKey(60, "gl");
    public static KeyPrefix getGoodsDetail = new GoodsKey(60, "gd");
    public static KeyPrefix getMiaoshaGoodsStock = new GoodsKey(0, "gs");
}
