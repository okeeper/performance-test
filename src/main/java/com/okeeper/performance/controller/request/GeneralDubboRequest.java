package com.okeeper.performance.controller.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 通用dubbo的测试入参request
 * @author zhangyue1
 */
@Data
public class GeneralDubboRequest implements Serializable {
    /**
     * 接口名
     */
    @NotEmpty
    private String interfaceName;

    /**
     * 方法名
     */
    @NotEmpty
    private String method;

    /**
     * 参数类型 如 java.lang.String
     */
    private String paramType;

    /**
     * 参数json
     */
    private String paramValue;

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
    @NotEmpty
    private String taskName;

    /**
     * 指定dubbo接口ip端口
     * 例如： 192.168.1.131:32101
     */
    private String referenceUrl;

    /**
     * 注册中心地址
     */
    private String registryAddress;

    @Min(1)
    @NotNull
    private Integer fork;

    @Min(1)
    @NotNull
    private Integer timeout;

    private List<DynamicRow> dynamicRow;


    private String responseClassName;

    /**
     * SpEL 表达式
     */
    private String spEL;

}
