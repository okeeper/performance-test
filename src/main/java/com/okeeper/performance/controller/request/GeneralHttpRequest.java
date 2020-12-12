package com.okeeper.performance.controller.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zhangyue1
 */
@Data
public class GeneralHttpRequest implements Serializable {
    /**
     * 接口名
     */
    @NotEmpty
    private String url;

    /**
     * 请求类型
     * POST or GET
     */
    @NotEmpty
    private String method;

    /**
     * post请求头部
     */
    private String headerStr;

    /**
     * get请求参数
     */
    private String paramsStr;

    /**
     * post请求body
     */
    private String requestBody;

    /**
     * 线程数
     */
    @Min(1)
    @NotNull
    private Integer threads;

    /**
     * 预热次数
     */
    @Min(0)
    @NotNull
    private Integer warmupTimes;

    /**
     * 实际有效测量次数
     */
    @Min(1)
    @NotNull
    private Integer times;

    /**
     * 任务名称
     */
    private String taskName;

    private String resultClass;

    @Min(1)
    @NotNull
    private Integer fork;


}
