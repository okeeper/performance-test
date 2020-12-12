package com.okeeper.performance.utils;

import com.okeeper.performance.controller.request.DynamicRow;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
public class DynamicParameterWrap {

    public static final String RANDOM_INT = "__RANDOM_INT";

    public static final String RANDOM_STRING = "__RANDOM_STRING";

    public static final String DYNAMIC_PARAM_REGEX = "\\$\\{(\\w+)\\}";

    public static Pattern pattern = Pattern.compile(Joiner.on("|").join(RANDOM_INT, RANDOM_STRING, DYNAMIC_PARAM_REGEX));


    public static String wrap(String param) {
        return containsDynamicKey(param) ? replace(param) : param;
    }

    public static List<DynamicValueSourceCursor> buildDynamicValueSourceListCursorList(List<DynamicRow> dynamicRows) {
        if(dynamicRows != null) {
            return dynamicRows.stream().filter(dynamicRow -> {
                return  (dynamicRow != null && StringUtils.isNotEmpty(dynamicRow.getName()) && StringUtils.isNotEmpty(dynamicRow.getValue()));
            }).map(dynamicRow -> {
                DynamicValueSourceCursor<String> dynamicValueSourceCursor = new DynamicValueSourceListCursor(dynamicRow.getName(), JSON.parseArray(dynamicRow.getValue()));
                return dynamicValueSourceCursor;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static Object[] wrapDubboParameters(Object []params, Type[] paramTypes, List<DynamicValueSourceCursor> dynamicValueSourceCursorList) {
        if(params != null) {
            Object []finalContents = new Object[params.length];
            for(int i=0;i<params.length;i++) {
                if(params[i] instanceof String && containsDynamicKey((String) params[i])) {
                    finalContents[i] = replaceAndConventType((String) params[i], paramTypes[i],dynamicValueSourceCursorList);
                }else {
                    finalContents[i] = params[i];
                }
            }
            return finalContents;
        }
        return null;
    }

    private static Object replaceAndConventType(String param, Type paramType, List<DynamicValueSourceCursor> dynamicValueSourceCursorList) {
        return JSON.parseObject(replaceWithDynamicParameters(param, dynamicValueSourceCursorList), paramType);
    }

    private static String replaceWithDynamicParameters(String param, List<DynamicValueSourceCursor> dynamicValueSourceCursorList) {
        String replacedParam = param
                .replaceAll("\"" + RANDOM_INT + "\"", RandomStringUtils.randomNumeric(10))
                .replaceAll(RANDOM_STRING, RandomStringUtils.randomAlphanumeric(20))
                ;
        String finalString = replacedParam;
        if(dynamicValueSourceCursorList != null && dynamicValueSourceCursorList.size() > 0) {
            Matcher matcher = pattern.matcher(replacedParam);
            final String[] cursoredValue = {null};
            StringBuffer sb = new StringBuffer();
            while(matcher.find()) {
                String paramName = matcher.group(1);
                dynamicValueSourceCursorList.forEach(dynamicValueSourceCursor -> {
                    if(dynamicValueSourceCursor.match(paramName)) {
                        if(cursoredValue[0] == null) {
                            cursoredValue[0] = dynamicValueSourceCursor.cursorValue();
                        }
                        matcher.appendReplacement(sb, cursoredValue[0]);
                    }
                });
            }
            matcher.appendTail(sb);
            finalString = sb.toString();
        }
        return finalString;
    }

    private static String replace(String param) {
        String replacedParam = param
                .replaceAll("\"" + RANDOM_INT + "\"", RandomStringUtils.randomNumeric(10))
                .replaceAll(RANDOM_STRING, RandomStringUtils.randomAlphanumeric(20));
        return replacedParam;
    }

    public static boolean containsDynamicKey(String param) {
        if(param != null) {
            Matcher matcher = pattern.matcher(param);
            return matcher.find();
        }
        return false;
    }

}
