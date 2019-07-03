package com.fhb.seckill.access;

import com.fhb.seckill.domain.MiaoshaUser;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/7/3
 * Time: 19:15
 *
 * @author hbfang
 */

public class UserContext {

    private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<>();

    public static void setUser(MiaoshaUser user) {
        userHolder.set(user);
    }

    public static MiaoshaUser getUser() {
        return userHolder.get();
    }
}
