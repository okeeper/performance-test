package com.okeeper.performance.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 支持嵌套的记时探针
 * @author zhangyue1
 */
@Slf4j
public class CostPin {

    /**
     * 为什么要用链表存储(准确来说的是栈)
     * <pre>
     * 为了支持嵌套切换，如ABC三个service都是不同的数据源
     * 其中A的某个业务要调B的方法，B的方法需要调用C的方法。一级一级调用切换，形成了链。
     * 传统的只设置当前线程的方式不能满足此业务需求，必须使用栈，后进先出。
     * </pre>
     */
    private static final ThreadLocal<Deque<Long>> COST_HOLDER = new NamedThreadLocal<Deque<Long>>("cost-pin") {
        @Override
        protected Deque<Long> initialValue() {
            return new ArrayDeque<>();
        }
    };

    /**
     * 开始计时
     */
    public static void start() {
        COST_HOLDER.get().push(System.currentTimeMillis());
    }

    /**
     * 获取耗时
     * @return long
     */
    public static long cost() {
        Deque<Long> deque = COST_HOLDER.get();
        try {
            Long currentStart = deque.poll();
            if(currentStart == null) {
                log.warn("The CostPin should call #start first.");
                return 0L;
            }
            return System.currentTimeMillis() - currentStart;
        } finally {
            if (deque.isEmpty()) {
                COST_HOLDER.remove();
            }
        }
    }
}
