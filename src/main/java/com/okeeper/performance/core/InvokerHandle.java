package com.okeeper.performance.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * 访问callable方法的handler
 * @author zhangyue1
 */
@Slf4j
public class InvokerHandle<T> implements Runnable {
    private Callable<T> callable;
    private Counter counter;
    private CountDownLatch latch;
    private  List<ResultAssert<T>> resultAssertList = Collections.emptyList();

    public InvokerHandle(CountDownLatch latch, Callable<T> callable, Counter counter) {
        this.latch = latch;
        this.callable = callable;
        this.counter = counter;
    }

    public InvokerHandle(CountDownLatch latch, Callable<T> callable, Counter counter, List<ResultAssert<T>> resultAssertList) {
        this.latch = latch;
        this.callable = callable;
        this.counter = counter;
        this.resultAssertList = resultAssertList;
    }

    @SneakyThrows
    @Override
    public void run() {
        try {
            boolean isContinue = false;
            do {
                long st = System.currentTimeMillis();
                try {
                    T result = callable.call();
                    doAssert(result);
                    isContinue = counter.countSuccess(result, st);
                } catch (Throwable e) {
                    isContinue = counter.countError(e, st);
                }
            } while (isContinue);
        } finally {
            latch.countDown();
        }
    }

    private void doAssert(T result) {
        for(ResultAssert<T> resultAssert : resultAssertList) {
            resultAssert.doAssert(result);
        }
    }
}