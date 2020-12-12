package com.okeeper.performance.controller.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PlaceOrderRequest implements Serializable {

    /**
     * 参数json
     */
    @NotEmpty
    private String arg;

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

}
