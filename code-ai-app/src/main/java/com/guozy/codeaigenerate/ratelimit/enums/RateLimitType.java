package com.guozy.codeaigenerate.ratelimit.enums;

/**
 * 限流类型
 */
public enum RateLimitType {
    /** 接口级别限流 */
    API,
    /** 用户级别限流 */
    USER,
    /** IP 级别限流 */
    IP
}