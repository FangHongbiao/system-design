package com.fhb.seckill.redis;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/7/3
 * Time: 10:24
 *
 * @author hbfang
 */

public class MiaoshaKey extends BasePrefix {


    public MiaoshaKey(String prefix) {
        super(prefix);
    }

    public MiaoshaKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static MiaoshaKey isGoodsOver = new MiaoshaKey("go");
    public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60, "mp");
    public static KeyPrefix getMiaoshaVerifyCode = new MiaoshaKey(300, "vc");
}
