package com.okeeper.performance.core;

public abstract class ResultAssert<T> {

    protected void doAssert(T result) {
        if(!conditional(result)) {
            throw new RuntimeException("assert fail. result = " + result);
        }
    }

    abstract boolean conditional(T result);
}
