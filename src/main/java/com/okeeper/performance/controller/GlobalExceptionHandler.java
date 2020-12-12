package com.okeeper.performance.controller;

import com.okeeper.performance.controller.response.WebResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author zhangyue1
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

   /**
    * 用于处理通用异常
    */
   @ExceptionHandler(BindException.class)
   public WebResult<Void> bindException(BindException e) {
       BindingResult bindingResult = e.getBindingResult();
       String errorMessage = "";
       for (FieldError fieldError : bindingResult.getFieldErrors()) {
           errorMessage += fieldError.getField() + ":"+ fieldError.getDefaultMessage() + ", ";
       }
       log.error("msg:" + errorMessage);
       return WebResult.error(500, errorMessage);
   }

    /**
     * 用于处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public WebResult<Void> bindException(Exception e) {
        log.error(e.getMessage(), e);
        return WebResult.error(500, e.getMessage());
    }
}