package com.fhb.seckill.redis;

/**
 * 暂时不设置过期时间
 *
 */
public class AccessKey extends BasePrefix {

    public AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static AccessKey access = new AccessKey(5, "access");
    public static AccessKey withExpire (int expire) {
        return new AccessKey(expire, "access");
    }

}
