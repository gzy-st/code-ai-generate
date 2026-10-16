package com.guozy.codeaigenerate.utils;

/**
 * 缓存 Key 生成工具
 */
public class CacheKeyUtils {

    private CacheKeyUtils() {
    }

    /**
     * 拼接缓存 Key
     *
     * @param prefix 前缀
     * @param parts  可变部分
     * @return 形如 prefix:part1:part2 的 key
     */
    public static String buildKey(String prefix, Object... parts) {
        if (parts == null || parts.length == 0) {
            return prefix;
        }
        StringBuilder sb = new StringBuilder(prefix);
        for (Object part : parts) {
            sb.append(':').append(part == null ? "null" : part.toString());
        }
        return sb.toString();
    }
}