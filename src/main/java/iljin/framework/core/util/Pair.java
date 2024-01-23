package iljin.framework.core.util;

import java.io.Serializable;

public class Pair<K, V> implements Serializable {

    private static final long serialVersionUID = 3952183860576834752L;
    private final K key;
    private final V value;

    public K getKey() { return key; }
    public V getValue() { return value; }

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public String toString() {
        return key + "=" + value;
    }
}
