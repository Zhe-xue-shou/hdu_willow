package com.hdu.hdufpga.entity.constant;

public class RedisConstant {
    //验证码前缀
    public final static String REDIS_VERIFICATION_CODE = "verificationCode:";
    //保持连接 身份->true
    //token的有效期
    public final static String REDIS_TTL_PREFIX = "ttl:";
    public final static Integer REDIS_TTL_LIMIT = 3 * 60; //connection must be checked within 3 minutes
    //用户connectionVO的索引 需要通过这个前缀找到connectionVO 有效时间12h
    public final static String REDIS_CONN_PREFIX = "conn:";
    //板卡使用计时器的shadow,单纯的倒计时器
    public final static String REDIS_CONN_SHADOW_PREFIX = "shadow:";
    public final static Integer REDIS_CONN_SHADOW_LIMIT = 30 * 60;

    public final static String REDIS_SESSION_PREFIX = "session:";

    public final static String REDIS_LOCK_PREFIX = "lock:";
    //板卡连接服务器的倒计时
    //板卡需要定期发心跳包/登录包刷新时间 否则认为板卡超时断连
    public final static String REDIS_BOARD_SERVER_PREFIX = "boardConnection:";
    public final static Integer REDIS_BOARD_SERVER_LIMIT = 2 * 60 + 30;
    // constant for storing use experiment start time
    public final static String REDIS_EXP_START_TIME_PREFIX = "expStartTime:";
}
