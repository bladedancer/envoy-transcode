package com.matthews.poc.transcode;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ObservableMap<K,V> implements Map<K,V> {
    public enum EventType{ ADDED, UPDATED, REMOVED }
    public record Event<K, V>(EventType type, K key, V value) {}

    private final Map<K,V> map;

    private final BroadcastProcessor<Event<K, V>> processor = BroadcastProcessor.create();

    public ObservableMap(Map<K,V> map) {
        Objects.requireNonNull(map, "Map cannot be null");
        this.map = map;
    }

    public Multi<Event<K, V>> observe() {
        return processor;
    }

    @Override
    public V put(K key, V value) {
        V old = map.put(key, value);
        processor.onNext(new Event<>(old == null ? EventType.ADDED : EventType.UPDATED, key, value));
        return old;
    }

    @Override
    public V remove(Object key) {
        V old = map.remove(key);
        processor.onNext(new Event<>(EventType.REMOVED, (K) key, old));
        return old;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        List<Event<K,V>> events = m.entrySet().stream()
                .map(entry -> new Event<K,V>(map.containsKey(entry.getKey()) ? EventType.UPDATED : EventType.ADDED, entry.getKey(), entry.getValue()))
                .toList();
        map.putAll(m);
        events.forEach(processor::onNext);
    }

    @Override
    public void clear() {
        Set<Map.Entry<K, V>> entries = map.entrySet();
        map.clear();
        for (Map.Entry<K, V> entry : entries) {
            processor.onNext(new Event<>(EventType.REMOVED, entry.getKey(), entry.getValue()));
        }
    }


    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

}
