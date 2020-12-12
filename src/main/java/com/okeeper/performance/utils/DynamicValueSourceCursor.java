package com.okeeper.performance.utils;

public interface DynamicValueSourceCursor<T> {

    boolean match(String param);

    String cursorValue();
}
