package com.fhb.seckill.util;

import java.util.UUID;

/**
 * @author aq7218
 */
public class UUIDUtil {
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
