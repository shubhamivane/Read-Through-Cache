package org.store.cache.redis;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.store.cache.Cache;
import org.store.cache.exceptions.CacheException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class RedisCache<K, V> extends Cache<K, V> {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final String CACHE_KEY_PREFIX = "redis:cache:";
  private Class<V> entityClass;
  private String cacheKey;
  private StringRedisTemplate redisTemplate;
  private Function<Long, String> longToString = String::valueOf;
  private Function<String, Long> stringToLong = Long::parseLong;
  private Function<Integer, String> integerToString = String::valueOf;
  private Function<String, Integer> stringToInteger = Integer::parseInt;
  private Function<Boolean, String> booleanToString = String::valueOf;
  private Function<String, Boolean> stringToBoolean = Boolean::parseBoolean;
  private Function<Float, String> floatToString = String::valueOf;
  private Function<String, Float> stringToFloat = Float::parseFloat;
  private Function<Double, String> doubleToString = String::valueOf;
  private Function<String, Double> stringToDouble = Double::parseDouble;


  @Override
  public boolean invalidate(K key) {
    return Boolean.TRUE.equals(redisTemplate.delete(getElementKey(key)));
  }

  @Override
  protected V _get(K key) {
    String value = redisTemplate.opsForValue().get(getElementKey(key));
    if(value == null) {
      V reloadedValue = reload(key);
      if(reloadedValue != null) {
        _set(key, reloadedValue);
        value = deserialize(reloadedValue);
      }
    }
    return serialize(value);
  }

  @Override
  protected boolean _set(K key, V value) {
    redisTemplate.opsForValue().set(getElementKey(key), deserialize(value), ttl, TimeUnit.SECONDS);
    return true;
  }

  @Override
  protected V reload(K key) {
    return reloader.apply(key);
  }

  @Override
  protected boolean isExpired(K key) {
    return !Boolean.TRUE.equals(redisTemplate.hasKey(getElementKey(key)));
  }

  private String getElementKey(K key) {
    return cacheKey + String.valueOf(key);
  }

  private V serialize(String value) {
    if (value == null || value.equals("")) {
      return null;
    }
    if(String.class.isAssignableFrom(entityClass)) {
      return (V) value;
    }
    if(Integer.class.isAssignableFrom(entityClass)) {
      return (V) stringToInteger.apply(value);
    }
    if(Float.class.isAssignableFrom(entityClass)) {
      return (V) stringToFloat.apply(value);
    }
    if(Double.class.isAssignableFrom(entityClass)) {
      return (V) stringToDouble.apply(value);
    }
    if(Boolean.class.isAssignableFrom(entityClass)) {
      return (V) stringToBoolean.apply(value);
    }
    if(Long.class.isAssignableFrom(entityClass)) {
      return (V) stringToLong.apply(value);
    }
    try {
      return objectMapper.readValue(value, entityClass);
    } catch(IOException e) {
      throw new CacheException(e.getMessage());
    }
  }

  private String deserialize(V value) {
    if(value == null) {
      return null;
    }
    if(String.class.isAssignableFrom(entityClass)) {
      return (String) value;
    } else if(Integer.class.isAssignableFrom(entityClass)) {
      return integerToString.apply((Integer) value);
    } else if(Float.class.isAssignableFrom(entityClass)) {
      return floatToString.apply((Float) value);
    } else if(Double.class.isAssignableFrom(entityClass)) {
      return doubleToString.apply((Double) value);
    } else if(Boolean.class.isAssignableFrom(entityClass)) {
      return booleanToString.apply((Boolean) value);
    } else if(Long.class.isAssignableFrom(entityClass)) {
      return longToString.apply((Long) value);
    } else {
      try {
        return objectMapper.writeValueAsString(value);
      } catch(IOException e) {
        throw new CacheException(e.getMessage());
      }
    }
  }

  public static class Builder<K, V> {
    private Long ttl;
    private String name;
    private Function<K, V> reloader;
    private StringRedisTemplate redisTemplate;
    private Class<V> entityClass;
    public Builder() {
    }

    public Builder<K, V> setTtl(Long ttl) {
      this.ttl = ttl;
      return this;
    }

    public Builder<K, V> setName(String name) {
      this.name = name;
      return this;
    }

    public Builder<K, V> setReloader(Function<K, V> function) {
      this.reloader = function;
      return this;
    }

    public Builder<K,V> setEntityClass(Class<V> entityClass) {
      this.entityClass = entityClass;
      return this;
    }

    public Builder<K, V> setRedisTemplate(StringRedisTemplate redisTemplate) {
      this.redisTemplate = redisTemplate;
      return this;
    }

    public Cache<K, V> build() {
      RedisCache<K, V> redisCache = new RedisCache<>();
      redisCache.redisTemplate = this.redisTemplate;
      redisCache.reloader = this.reloader;
      redisCache.name = this.name;
      redisCache.cacheKey = MessageFormat.format("{0}:{1}:", CACHE_KEY_PREFIX, this.name);
      redisCache.entityClass = this.entityClass;
      redisCache.ttl = this.ttl;
      return redisCache;
    }

  }
}
