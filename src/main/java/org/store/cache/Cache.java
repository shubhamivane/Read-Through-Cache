package org.store.cache;

import java.io.IOException;
import java.util.function.Function;

public abstract class Cache<K,V> {
    protected String name;
    protected Long ttl = Long.MAX_VALUE;
    protected Function<K, V> reloader;

    public V get(K key) {
        V value = _get(key);
        return value;
    }

    public boolean set(K key, V value) {
        return _set(key, value);
    }

    public abstract boolean invalidate(K key);
    protected abstract V _get(K key);
    protected abstract boolean _set(K key, V value);
    protected abstract V reload(K key);
    protected abstract boolean isExpired(K key);
}
