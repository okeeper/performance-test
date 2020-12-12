package com.okeeper.performance.core;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class Counter {


    private int threads;

    private int times;

    private int warmupTimes;

    private long startTimeMillis;

    private long finishTimeMillis;
    /**
     * 总次数
     */
    private AtomicLong totalTimes = new AtomicLong();
    /**
     * 实际测量次数 = 成功次数 + 失败次数
     */
    private AtomicLong measureTimes = new AtomicLong();
    /**
     * 成功次数
     */
    private AtomicLong successTimes = new AtomicLong();
    /**
     * 失败次数
     */
    private AtomicLong failTimes = new AtomicLong();
    /**
     * 总耗时
     */
    private AtomicLong costSum = new AtomicLong();
    /**
     * 成功请求耗时集合
     */
    private Vector<Long> successCostList = new Vector<>();
    private long totalCost;

    public Counter(int threads, int warmupTimes, int times) {
        this.threads = threads;
        this.times = times;
        this.warmupTimes = warmupTimes;
    }

    /**
     * 统计
     *
     * @param result          结果
     * @param startTimeMillis 开始时间
     * @return 是否继续
     */
    public boolean countSuccess(Object result, long startTimeMillis) {
        long cost = System.currentTimeMillis() - startTimeMillis;
        long currentTimes = totalTimes.incrementAndGet();
        if (currentTimes > warmupTimes) {
            measureTimes.incrementAndGet();
            successTimes.incrementAndGet();
            costSum.addAndGet(cost);
            successCostList.add(cost);
        }
        log.info(">>> call success, times={}, cost=【{}ms】", currentTimes, cost);
        //判断是否完成了压测次数
        return currentTimes < (warmupTimes + times);
    }

    /**
     * 统计
     *
     * @param e               异常
     * @param startTimeMillis 开始时间
     * @return 是否继续
     */
    public boolean countError(Throwable e, long startTimeMillis) {
        long cost = System.currentTimeMillis() - startTimeMillis;
        long currentTimes = totalTimes.incrementAndGet();
        if (currentTimes > warmupTimes) {
            measureTimes.incrementAndGet();
            failTimes.incrementAndGet();
            costSum.addAndGet(cost);
        }
        log.info(">>> call error, times={}, cost=【{}ms】", currentTimes, cost, e);
        return currentTimes < (warmupTimes + times);
    }

    public void start() {
        startTimeMillis = System.currentTimeMillis();
    }

    public void end() {
        finishTimeMillis = System.currentTimeMillis();
        totalCost = (finishTimeMillis - startTimeMillis);
    }

    public PerformanceResult getResult() {
        PerformanceResult performanceResult = new PerformanceResult();
        performanceResult.setThreads(threads);
        performanceResult.setTotalCost(totalCost);
        performanceResult.setTotalTimes(totalTimes.get());
        performanceResult.setMeasureTimes(measureTimes.get());
        performanceResult.setSuccessTimes(successTimes.get());
        performanceResult.setFailTimes(failTimes.get());

        float throughput = measureTimes.get() * 1.0f / (totalCost * 1.0f / 1000);
        performanceResult.setThroughput(throughput);

        double averageTime = successCostList.stream().mapToLong(Long::longValue).average().orElse(0D);
        performanceResult.setAverageTime(Double.valueOf(averageTime).intValue());

        //成功率
        performanceResult.setSuccessRate(successTimes.get() * 1.0f / measureTimes.get());

        if(successCostList.size() > 0) {
            List<Long> costList = new ArrayList<>(successCostList);
            //从小到大自然排序
            costList.sort(Comparator.naturalOrder());
            //最大耗时
            performanceResult.setMaxCost(costList.size() > 0 ? costList.get(costList.size() - 1) : 0);
            if (costList.size() > 2) {
                //剔除最小值，最大值
                costList.remove(0);
                costList.remove(costList.size() - 1);

            }
            long tp99Value = costList.get(new Float((costList.size() - 1) * 0.99f).intValue());
            performanceResult.setCostTimeTp99(tp99Value);

            long tp95Value = costList.get(new Float((costList.size() - 1) * 0.95f).intValue());
            performanceResult.setCostTimeTp95(tp95Value);

            long tp90Value = costList.get(new Float((costList.size() - 1) * 0.90f).intValue());
            performanceResult.setCostTimeTp90(tp90Value);

            long tp50Value = costList.get(new Float((costList.size() - 1) * 0.50f).intValue());
            performanceResult.setCostTimeTp50(tp50Value);
        }
        return performanceResult;
    }

}