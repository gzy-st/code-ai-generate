package com.guozy.codeaigenerate.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;

/**
 * 多级缓存：Caffeine（本地 L1）+ Redis（分布式 L2）
 */
@Slf4j
public class MultiLevelCache extends AbstractValueAdaptingCache {

    private final String name;
    private final Cache<Object, Object> localCache;
    private final RedisCache redisCache;

    public MultiLevelCache(String name, Cache<Object, Object> localCache, RedisCache redisCache, boolean allowNullValues) {
        super(allowNullValues);
        this.name = name;
        this.localCache = localCache;
        this.redisCache = redisCache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    @Nullable
    protected Object lookup(Object key) {
        // 1. 查本地缓存
        Object localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            log.debug("多级缓存[{}] 命中本地缓存, key={}", name, key);
            return localValue;
        }
        // 2. 查 Redis（redisCache.get 返回 ValueWrapper，需解包）
        org.springframework.cache.Cache.ValueWrapper redisWrapper = redisCache.get(key);
        if (redisWrapper != null) {
            Object redisValue = redisWrapper.get();
            log.debug("多级缓存[{}] 命中 Redis 缓存, key={}", name, key);
            localCache.put(key, redisValue);
            return redisValue;
        }
        return null;
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        Object storeValue = toStoreValue(value);
        localCache.put(key, storeValue);
        redisCache.put(key, storeValue);
    }

    @Override
    public void evict(Object key) {
        localCache.invalidate(key);
        redisCache.evict(key);
    }

    @Override
    public void clear() {
        localCache.invalidateAll();
        redisCache.clear();
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object cached = lookup(key);
        if (cached != null) {
            return (T) fromStoreValue(cached);
        }
        try {
            T value = valueLoader.call();
            put(key, value);
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }
}