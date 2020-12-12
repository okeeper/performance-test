package com.okeeper.performance.controller;

import com.alibaba.fastjson.JSON;
import com.okeeper.performance.common.Constants;
import com.okeeper.performance.controller.request.GeneralDubboRequest;
import com.okeeper.performance.controller.request.GeneralHttpRequest;
import com.okeeper.performance.controller.response.WebResult;
import com.okeeper.performance.utils.PerformanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.dubbo.common.utils.FieldUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;


/**
 * 接口性能测试
 * @author zhangyue1
 */
@Slf4j
@RestController
@RequestMapping("/performance")
public class PerformanceController {


    @Autowired
    private PerformanceUtils performanceUtils;


    /**
     * dubbo通用接口测试
     * @return
     */
    @ResponseBody
    @RequestMapping("/generalTest")
    public String generalTest(@Valid GeneralDubboRequest request) {
        try {
            String resultFileName = performanceUtils.addDubboTask(request);
            return "success. 结果输出文件:" + resultFileName;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return e.toString();
        }
    }


    /**
     * Http通用接口测试
     * @return
     */
    @ResponseBody
    @RequestMapping("/generalHttp")
    public String generalHttp(@Valid GeneralHttpRequest request) {
        try {

            String resultFileName = performanceUtils.addHttpTask(request.getTaskName(),
                    request.getUrl(),
                    request.getMethod(),
                    JSON.parseObject(StringUtils.isNotEmpty(request.getHeaderStr()) ? request.getHeaderStr() : null, Map.class),
                    JSON.parseObject(StringUtils.isNotEmpty(request.getParamsStr()) ? request.getParamsStr() : null, Map.class),
                    request.getRequestBody(),
                    request.getResultClass(),
                    request.getThreads(),
                    request.getWarmupTimes(),
                    request.getTimes(),
                    request.getFork());
            return "success. 结果输出文件:" + resultFileName;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return e.toString();
        }
    }


    /**
     * 异步多个文件上传和表单数据
     * @param request
     * @param upload 采用数组接收
     * @return
     */
    @RequestMapping(value = "/uploadJar", method = RequestMethod.POST)
    public WebResult<String> uploadJar(HttpServletRequest request, MultipartFile[] upload) throws IOException {
        String path = Constants.EXT_JAR_PATH;
        //创建文件对象
        File file = new File(path);

        for (MultipartFile multipartFile : upload) {
            //获取上传文件的名称
            String filename = multipartFile.getOriginalFilename();
            if(filename == null || filename.lastIndexOf(".jar") == -1) {
                throw new IllegalArgumentException("only supports jar file.");
            }
            //上传文件
            File uploadFile = new File(file, filename);
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), uploadFile);
            log.info("uploadJar success. filePath={}", uploadFile.getPath());
        }

        return WebResult.success("success");
    }
}
