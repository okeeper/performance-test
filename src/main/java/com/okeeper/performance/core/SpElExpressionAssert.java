package com.okeeper.performance.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Arrays;

/**
 * 采用 Spring Expression Language (SpEL) expression来断言返回结果
 * https://docs.spring.io/spring-framework/docs/4.2.x/spring-framework-reference/html/expressions.html
 * @author zhangyue1
 */
@Slf4j
public class SpElExpressionAssert extends ResultAssert<Object> {

    private Expression exp;

    public SpElExpressionAssert(String spEL) {
        //创建ExpressionParser解析表达式
        ExpressionParser parser = new SpelExpressionParser();
        this.exp = parser.parseExpression(spEL);
    }

    @Override
    boolean conditional(Object result) {
        if(result != null) {
            StandardEvaluationContext ctx = new StandardEvaluationContext();
            ctx.setVariable("result", result);
            Boolean condition = exp.getValue(ctx, Boolean.class);
            return condition != null ? condition : false;
        }else {
            return false;
        }

    }

    public static void main(String[] args) {

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        PerformanceResult result = new PerformanceResult();
        result.setThreads(1000);
        result.setTestList(Arrays.asList("aaaa","bbb"));
        ctx.setVariable("result", result);

        String el = "#result.threads == 1000 and #result.testList[1] != null";
        //创建ExpressionParser解析表达式
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(el);

        log.info(">>>>" + exp.getValue(ctx));
    }
}
