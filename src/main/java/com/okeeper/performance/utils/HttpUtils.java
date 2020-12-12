package com.okeeper.performance.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
public class HttpUtils {

    @Autowired
    RestTemplate restTemplate;

    public <T> T post(String url, Map<String, Object> headerMaps, String requestBody, Class<T> resultClass){
        HttpHeaders httpHeaders =new HttpHeaders();
        if(headerMaps != null) {
            for(String key: headerMaps.keySet()) {
                httpHeaders.put(key, Arrays.asList(headerMaps.get(key).toString()));
            }
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, httpHeaders);
        ResponseEntity<T> response = restTemplate.postForEntity( url, request , resultClass);
        //log.info("==post {}, requestBody {}, response {}", url, requestBody, response);
        return response.getBody();
    }

    public <T> T get(String url, Map<String, Object> params, Class<T> resultClass){
        T result = null;
        if(params != null && !params.isEmpty() && resultClass != null) {
            result = restTemplate.getForObject(url, resultClass, params);
        }else   {
            result = restTemplate.getForObject(url, resultClass);
        }
        //log.info("==get {}, params {}, result {}", url, JSON.toJSONString(params), result);
        return result;
    }
}
