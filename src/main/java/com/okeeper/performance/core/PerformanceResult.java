package com.okeeper.performance.core;

import com.okeeper.performance.utils.MessageUtils;
import lombok.Data;

import java.util.List;


@Data
public class PerformanceResult {

    /**
     * 线程数
     */
    private int threads;

    /**
     * 处理总耗时
     */
    private long totalCost;

    /**
     * 调用总次数
     */
    private long totalTimes;

    /**
     * 有效测量次数
     */
    private long measureTimes;

    /**
     * 预热测试
     */
    private long warmupTimes;

    /**
     * 有效测量次数的成功次数
     */
    private long successTimes;

    /**
     * 有效测量次数的失败次数
     */
    private long failTimes;

    /**
     * 吞吐量 ops/s
     * 包含失败的请求
     */
    private double throughput;

    /**
     * 平均耗时 ms
     */
    private double averageTime;

    /**
     * 将所有耗时从小到大排序，第99%的下标对应的值为tp99
     */
    private double costTimeTp99;

    /**
     * 将所有耗时从小到大排序，第95%的下标对应的值为tp95
     */
    private double costTimeTp95;

    /**
     * 将所有耗时从小到大排序，第90%的下标对应的值为tp90
     */
    private double costTimeTp90;

    /**
     * 将所有耗时从小到大排序，第50%的下标对应的值为tp50
     */
    private double costTimeTp50;

    /**
     * 成功率
     */
    private float successRate;

    /**
     * 最大耗时
     */
    private long maxCost;

    private List<String> testList;

    @Override
    public String toString() {
        String format =
                "Threads: {} \n" +
                "Total Times: {} cnt\n" +
                "Measure Times: {} cnt\n" +
                "Successful Times: {} cnt\n" +
                "Failed Times: {} cnt\n" +
                "Throughput: {} qps/s\n" +
                "Successful Rate: {} %\n" +
                "Total Cost: {} ms\n" +
                "Max Cost: {} ms\n" +
                "Average Time: {} ms\n" +
                "Cost Time of Tp99: {} ms\n" +
                "Cost Time of Tp95: {} ms\n" +
                "Cost Time of Tp90: {} ms\n" +
                "Cost Time of Tp50: {} ms\n"
                ;
        return MessageUtils.format(format,
                threads,
                totalTimes,
                measureTimes,
                successTimes,
                failTimes,
                throughput,
                successRate * 100,
                totalCost,
                maxCost,
                averageTime,
                costTimeTp99,
                costTimeTp95,
                costTimeTp90,
                costTimeTp50);
    }
}
