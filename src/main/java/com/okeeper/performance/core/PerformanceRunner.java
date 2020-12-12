package com.okeeper.performance.core;

import com.okeeper.performance.common.Constants;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 性能测试Runner
 * @author zhangyue1
 */
@Slf4j
public class PerformanceRunner<T> implements Runnable {

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private Callable<T> callable;

    private int threads;

    private int times;

    private int warmupTimes;

    private int fork = 1;

    private String resultFileName;

    private List<ResultAssert<T>> resultAssertList = Lists.newArrayList();

    private String taskName;

    public PerformanceRunner(Callable<T> callable, int threads, int warmupTimes, int times, int fork, String taskName) {
        this.callable = callable;
        this.threads = threads;
        this.times = times;
        this.warmupTimes = warmupTimes;
        this.taskName = taskName;
        this.fork = fork;
        this.resultFileName = Constants.LOG_PATH + "/report/" + taskName + "." + new SimpleDateFormat(Constants.DATE_FORMAT).format(new Date());
    }

    public PerformanceRunner(Callable<T> callable, int threads, int warmupTimes, int times, String taskName) {
        this.callable = callable;
        this.threads = threads;
        this.times = times;
        this.warmupTimes = warmupTimes;
        this.taskName = taskName;
        this.resultFileName = Constants.LOG_PATH + "/report/" + taskName + "." + new SimpleDateFormat(Constants.DATE_FORMAT).format(new Date());
    }

    public void addResultAssert(ResultAssert<T> resultAssert) {
        if(resultAssert != null) {
            this.resultAssertList.add(resultAssert);
        }
    }

    public void start() throws InterruptedException, IOException {
        List<PerformanceResult> countResultList = Lists.newArrayList();
        for(int forkTime = 1; forkTime <= fork ;forkTime++) {
            Counter counter = new Counter(threads, warmupTimes, times);

            log.info("==========================start for forTime 【{}】...", forkTime);
            CountDownLatch countDownLatch =  new CountDownLatch(threads);
            counter.start();
            for (int i = 0; i < threads; i++) {
                executorService.execute(new InvokerHandle(countDownLatch, callable, counter, resultAssertList));
            }

            log.info("==========================waiting for forkTime 【{}】...", forkTime);
            countDownLatch.await();
            counter.end();

            PerformanceResult result = counter.getResult();
            countResultList.add(result);
            log.info("==========================Result of forkTime 【{}】=======================", forkTime);
            log.info("\n"+result.toString());
        }
        executorService.shutdown();

        String finalResultString = buildResultStringForFinalTime(countResultList);
        log.info("==================================================================");
        log.info("=========================PerformanceResult========================");
        log.info("==================================================================");
        log.info("=========================<<<<{}>>>=========================", taskName);
        log.info("{}", finalResultString);
        log.info("=========================<<<<{}>>>>=========================end", taskName);

        FileUtils.write(new File(resultFileName), finalResultString, true);
        log.info("<<<<{}>>>> result save in: {}", taskName, resultFileName);
    }

    private PerformanceResult mergeResult(List<PerformanceResult> resultList) {
        if(resultList.size() == 1) {
            return resultList.get(0);
        }
        PerformanceResult performanceResult = new PerformanceResult();
        performanceResult.setThreads(resultList.get(0).getThreads());

        performanceResult.setWarmupTimes(resultList.stream().mapToLong(PerformanceResult::getWarmupTimes).sum());
        performanceResult.setTotalTimes(resultList.stream().mapToLong(PerformanceResult::getTotalTimes).sum());
        performanceResult.setFailTimes(resultList.stream().mapToLong(PerformanceResult::getFailTimes).sum());
        performanceResult.setSuccessTimes(resultList.stream().mapToLong(PerformanceResult::getSuccessTimes).sum());
        performanceResult.setMeasureTimes(resultList.stream().mapToLong(PerformanceResult::getMeasureTimes).sum());
        performanceResult.setTotalCost(resultList.stream().mapToLong(PerformanceResult::getTotalCost).sum());

        Double avgSuccessRate = resultList.stream().mapToDouble(PerformanceResult::getSuccessRate).average().orElse(0f);
        performanceResult.setSuccessRate(avgSuccessRate.floatValue());

        Double avgAverageTime = resultList.stream().mapToDouble(PerformanceResult::getAverageTime).average().orElse(0);
        performanceResult.setAverageTime(avgAverageTime.floatValue());

        Double maxCost = resultList.stream().mapToDouble(PerformanceResult::getMaxCost).average().orElse(0);
        performanceResult.setMaxCost(maxCost.intValue());

        Double avgThroughput = resultList.stream().mapToDouble(PerformanceResult::getThroughput).average().orElse(0);
        performanceResult.setThroughput(avgThroughput.floatValue());

        Double avgTp99 = resultList.stream().mapToDouble(PerformanceResult::getCostTimeTp99).average().orElse(0);
        performanceResult.setCostTimeTp99(avgTp99.floatValue());

        Double avgTp95 = resultList.stream().mapToDouble(PerformanceResult::getCostTimeTp95).average().orElse(0);
        performanceResult.setCostTimeTp95(avgTp95.floatValue());

        Double avgTp90 = resultList.stream().mapToDouble(PerformanceResult::getCostTimeTp90).average().orElse(0);
        performanceResult.setCostTimeTp90(avgTp90.floatValue());

        Double avgTp50 = resultList.stream().mapToDouble(PerformanceResult::getCostTimeTp50).average().orElse(0);
        performanceResult.setCostTimeTp50(avgTp50.floatValue());

        return performanceResult;
    }


    private String buildResultStringForFinalTime(List<PerformanceResult> performanceResultList) {
        PerformanceResult finalResult = mergeResult(performanceResultList);
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        String head = Joiner.on(" | ").join(
                Arrays.asList(
                        "Desc",
                        "Threads",
                        "Total Times (cnt)",
                        "Total Cost (ms)",
                        "Measure Times (cnt)",
                        "Successful Times(cnt)",
                        "Failed Times (cnt)",
                        "Throughput (qps/s)",
                        "Successful Rate (%)",
                        "Max Cost (ms)",
                        "Average Time (ms)",
                        "Tp99 (ms)",
                        "Tp95 (ms)",
                        "Tp90 (ms)",
                        "Tp50 (ms)"
                ))
                ;
        sb.append(head).append("\n");
        if(performanceResultList.size() > 1) {
            int i = 1;
            for (PerformanceResult result : performanceResultList) {
                sb.append(
                        Joiner.on("|").join(
                                Arrays.asList(
                                        "Result of Fork [" + i++ + "]: ",
                                        result.getThreads(),
                                        result.getTotalTimes(),
                                        result.getTotalCost(),
                                        result.getMeasureTimes(),
                                        result.getSuccessTimes(),
                                        result.getFailTimes(),
                                        result.getThroughput(),
                                        result.getSuccessRate() * 100,
                                        result.getMaxCost(),
                                        result.getAverageTime(),
                                        result.getCostTimeTp99(),
                                        result.getCostTimeTp95(),
                                        result.getCostTimeTp90(),
                                        result.getCostTimeTp50()
                                ))
                ).append("\n");
            }
        }

            sb.append(
                    Joiner.on(" | ").join(
                            Arrays.asList(
                                    "Final Result: ",
                                    finalResult.getThreads(),
                                    finalResult.getTotalTimes(),
                                    finalResult.getTotalCost(),
                                    finalResult.getMeasureTimes(),
                                    finalResult.getSuccessTimes(),
                                    finalResult.getFailTimes(),
                                    finalResult.getThroughput(),
                                    finalResult.getSuccessRate() * 100,
                                    finalResult.getMaxCost(),
                                    finalResult.getAverageTime(),
                                    finalResult.getCostTimeTp99(),
                                    finalResult.getCostTimeTp95(),
                                    finalResult.getCostTimeTp90(),
                                    finalResult.getCostTimeTp50()
                            ))
            ).append("\n");
        return sb.toString();
    }

    @SneakyThrows
    @Override
    public void run() {
        try {
            start();
        }catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    public String getResultFileName() {
        return resultFileName;
    }
}
