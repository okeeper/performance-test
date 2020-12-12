package com.okeeper.performance.controller.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class DynamicRow implements Serializable {
    private String name;
    private String value;
}
