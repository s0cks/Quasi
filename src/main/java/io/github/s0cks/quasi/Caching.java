package io.github.s0cks.quasi;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

final class Caching{
    private Caching(){}

    public static final int CACHE_SIZE = Integer.valueOf(System.getProperty(Caching.class.getName() + ".cacheSize", "127"));

    public static <K, V> Cache<K, V> newLRU(){
        return new LRUCache<>(CACHE_SIZE);
    }

    public static interface Cache<K, V>
    extends Iterable<Map.Entry<K, V>>{
        public V get(K key);
        public V put(K key, V value);
        public boolean containsKey(K key);
        public int size();
    }

    private static final class LRUCache<K, V>
    extends LinkedHashMap<K, V>
    implements Cache<K, V>{
        private final int capacity;

        private LRUCache(int capacity){
            super(capacity + 1, 0.75F, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> entry){
            return this.size() > this.capacity;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return this.entrySet().iterator();
        }
    }
}