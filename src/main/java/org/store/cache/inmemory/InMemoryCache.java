package org.store.cache.inmemory;

import org.store.cache.Cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class InMemoryCache<K, V> extends Cache<K, V> {
  private final ConcurrentMap<K, V> cache = new ConcurrentHashMap<>();
  private final ConcurrentMap<K, Long> ttlMap = new ConcurrentHashMap<>();

  @Override
  public boolean invalidate(K key) {
    return false;
  }

  @Override
  protected V _get(K key) {
    return cache.get(key);
  }

  @Override
  protected boolean _set(K key, V value) {
    cache.put(key, value);
    ttlMap.put(key, System.currentTimeMillis() + ttl);
    return true;
  }

  @Override
  protected V reload(K key) {
    V value = reloader.apply(key);
    _set(key, value);
    return value;
  }

  @Override
  protected boolean isExpired(K key) {
    return false;
  }


  public static class Builder<K, V> {
    InMemoryCache<K, V> inMemoryCache;
    Builder() {
      inMemoryCache = new InMemoryCache<>();
    }

    public Builder<K, V> setTtl(Long ttl) {
      inMemoryCache.ttl = ttl;
      return this;
    }

    public Builder<K, V> setName(String name) {
      inMemoryCache.name = name;
      return this;
    }

    public Builder<K, V> setReloader(Function<K, V> function) {
      inMemoryCache.reloader = function;
      return this;
    }

    public InMemoryCache<K, V> build() {
      return inMemoryCache;
    }

  }
}
