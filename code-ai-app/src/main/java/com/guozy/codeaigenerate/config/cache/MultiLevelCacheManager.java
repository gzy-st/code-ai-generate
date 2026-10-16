package com.guozy.codeaigenerate.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多级缓存管理器（Caffeine L1 + Redis L2）
 */
public class MultiLevelCacheManager extends AbstractCacheManager {

    private final RedisCacheManager redisCacheManager;
    private final ConcurrentHashMap<String, MultiLevelCache> cacheMap = new ConcurrentHashMap<>();

    private static final long LOCAL_MAX_SIZE = 1000;
    private static final Duration LOCAL_EXPIRE = Duration.ofMinutes(10);

    public MultiLevelCacheManager(RedisCacheManager redisCacheManager) {
        this.redisCacheManager = redisCacheManager;
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        return cacheMap.values();
    }

    @Override
    protected Cache getMissingCache(String name) {
        return cacheMap.computeIfAbsent(name, n -> {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> localCache = Caffeine.newBuilder()
                    .maximumSize(LOCAL_MAX_SIZE)
                    .expireAfterWrite(LOCAL_EXPIRE)
                    .build();
            // RedisCacheManager 会懒创建 RedisCache
            RedisCache redisCache = (RedisCache) redisCacheManager.getCache(n);
            return new MultiLevelCache(n, localCache, redisCache, false);
        });
    }
}