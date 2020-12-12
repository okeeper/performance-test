package com.okeeper.performance.utils;

import com.okeeper.performance.controller.request.GeneralDubboRequest;
import com.okeeper.performance.core.PerformanceRunner;
import com.okeeper.performance.core.ResultAssert;
import com.okeeper.performance.core.SpElExpressionAssert;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PerformanceUtils {

    /**
     * 线程池
     */
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    private DubboInterfaceUtils dubboInterfaceUtils;

    @Autowired
    private HttpUtils httpUtils;

    public String addDubboTask(GeneralDubboRequest request) throws ClassNotFoundException {
        String taskName = request.getTaskName();
        if (StringUtils.isEmpty(taskName)) {
            //默认task名称
            taskName = request.getInterfaceName().substring(request.getInterfaceName().lastIndexOf(".") + 1) + "#" + request.getMethod();
        }

        GenericService genericService = dubboInterfaceUtils.getInterface(request.getInterfaceName(), request.getRegistryAddress(), request.getReferenceUrl(), request.getTimeout());

        List<DynamicValueSourceCursor> dynamicValueSourceCursorList = DynamicParameterWrap.buildDynamicValueSourceListCursorList(request.getDynamicRow());

        MethodArgument methodArgument = dubboInterfaceUtils.getMethodArgument(request.getInterfaceName(), request.getMethod(), request.getParamType(), request.getParamValue());

        PerformanceRunner performanceRunner = new PerformanceRunner<>(
                () -> dubboInterfaceUtils.invoke(genericService,
                        request.getMethod(),
                        methodArgument.getParameterTypes(),
                        DynamicParameterWrap.wrapDubboParameters(methodArgument.getActualParameterValues(), methodArgument.getActualParameterTypes(), dynamicValueSourceCursorList),
                        methodArgument.getReturnType()),
                request.getThreads(),
                request.getWarmupTimes(),
                request.getTimes(),
                request.getFork(),
                taskName
        );
        if(StringUtils.isNotEmpty(request.getSpEL())) {
            //添加结果断言
            performanceRunner.addResultAssert(new SpElExpressionAssert(request.getSpEL()));
        }
        executorService.execute(performanceRunner);
        return performanceRunner.getResultFileName();
    }




    public String addHttpTask(String taskName, String url, String method, Map<String, Object> headerMaps, Map<String, Object> params, String requestBody,
                              String resultClassString, Integer threads, Integer warmupTimes, Integer times, Integer fork) throws ClassNotFoundException {
        if (StringUtils.isEmpty(taskName)) {
            //默认task名称
            taskName = url;
        }

        Class resultClass = String.class;
        if (StringUtils.isNotEmpty(resultClassString)) {
            resultClass = Class.forName(resultClassString);
        }
        Class finalResultClass = resultClass;

        HttpMethod httpMethod = HttpMethod.valueOf(method);

        PerformanceRunner<Object> performanceRunner = new PerformanceRunner<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (HttpMethod.GET.equals(httpMethod)) {
                    return httpUtils.get(URLUtils.chooseRandomUrl(url), params, finalResultClass);
                }
                return httpUtils.post(URLUtils.chooseRandomUrl(url), headerMaps,  DynamicParameterWrap.wrap(requestBody), finalResultClass);
            }
        },
                threads, warmupTimes, times,fork,
                taskName
        );


        executorService.execute(performanceRunner);
        return performanceRunner.getResultFileName();
    }

    public <T> void addCallableTask(String taskName, Callable<T> callable, Integer threads, Integer warmupTimes, Integer times, ResultAssert<T> resultAssert) {
        PerformanceRunner<T> performanceRunner = new PerformanceRunner<>(
                callable,
                threads,
                warmupTimes,
                times,1,
                taskName);
        //添加结果断言
        performanceRunner.addResultAssert(resultAssert);
        executorService.execute(performanceRunner);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}
