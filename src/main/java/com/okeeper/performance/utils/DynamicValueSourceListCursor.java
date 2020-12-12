package com.okeeper.performance.utils;


import java.util.List;

public class DynamicValueSourceListCursor<T> implements DynamicValueSourceCursor {

    private List<T> valueList;

    private String paramName;

    private int currentIndex;

    public DynamicValueSourceListCursor(String paramName, List<T> valueList) {
        this.paramName = paramName;
        this.valueList = valueList;
    }

    @Override
    public boolean match(String param) {
        return paramName.equals(param);
    }

    @Override
    public String cursorValue() {
        return valueList.get(currentIndex++ % valueList.size()).toString();
    }
}
