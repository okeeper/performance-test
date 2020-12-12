package com.okeeper.performance.utils;

import com.alibaba.spring.util.BeanFactoryUtils;
import com.okeeper.performance.common.Constants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DubboInterfaceUtils implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    private ApplicationConfig getApplicationConfig() {
        return BeanFactoryUtils.getOptionalBean(applicationContext, Constants.APP_NAME, ApplicationConfig.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private ConcurrentHashMap<String, GenericService> cachedService = new ConcurrentHashMap<>();
    /**
     * 订阅一个rpc接口并缓存
     * @param interfaceName 接口名称
     * @return GenericService
     */
    public  GenericService getInterface(String interfaceName, String registryAddress, String referenceUrl, int timeoutMillsSeconds) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setApplication(new ApplicationConfig(Constants.APP_NAME));
        reference.setInterface(interfaceName);

        if(StringUtils.isNotEmpty(registryAddress)){
            registryAddress = "nacos://127.0.0.1";
        }

        reference.setRegistry(new RegistryConfig(registryAddress));
        if(StringUtils.isNotEmpty(referenceUrl)) {
            reference.setUrl(referenceUrl+ "/" +interfaceName);
        }

        reference.setTimeout(timeoutMillsSeconds);
        reference.setGeneric(true);

        String cacheKey = MY_KEY_GENERATOR.generateKey(reference);
        GenericService genericService = cachedService.get(cacheKey);

        if(genericService == null) {
            cachedService.putIfAbsent(cacheKey, reference.get());
            return cachedService.get(cacheKey);
        }else {
            return genericService;
        }
    }

    public static final ReferenceConfigCache.KeyGenerator MY_KEY_GENERATOR = referenceConfig -> {
        String iName = referenceConfig.getInterface();
        if (StringUtils.isBlank(iName)) {
            Class<?> clazz = referenceConfig.getInterfaceClass();
            iName = clazz.getName();
        }
        if (StringUtils.isBlank(iName)) {
            throw new IllegalArgumentException("No interface info in ReferenceConfig" + referenceConfig);
        }

        StringBuilder ret = new StringBuilder();
        if (!StringUtils.isBlank(referenceConfig.getGroup())) {
            ret.append(referenceConfig.getGroup()).append("/");
        }
        ret.append(iName);
        if (!StringUtils.isBlank(referenceConfig.getVersion())) {
            ret.append(":").append(referenceConfig.getVersion());
        }
        if (!StringUtils.isBlank(referenceConfig.getUrl())) {
            ret.append(":").append(referenceConfig.getUrl());
        }
        return ret.toString();
    };



    public Object[] toArgObjects(String []parameterTypes, String []args) throws ClassNotFoundException {
        Object[] argObjects = new Object[parameterTypes.length];
        for(int i=0;i<parameterTypes.length;i++) {
            String parameterType = parameterTypes[i];
            Class parameterClass = Class.forName(parameterType);
            argObjects[i] = JSON.parseObject(args[i], parameterClass);
        }
        return argObjects;
    }

    public MethodArgument getMethodArgument(String interfaceName, String method, String paramType, String paramValue) {
        if(StringUtils.isNotEmpty(paramType) && StringUtils.isNotEmpty(paramValue)) {
            List<String> typeList = paramType.startsWith("[") ? JSON.parseArray(paramType, String.class) : Arrays.asList(paramType);
            List<String> valueList = paramValue.startsWith("[") ? JSON.parseArray(paramValue, String.class) : Arrays.asList(paramValue);
            String [] parameterTypes = typeList.toArray(new String[typeList.size()]);
            String [] parameterValues = valueList.toArray(new String[valueList.size()]);

            if(parameterTypes.length != parameterValues.length) {
                throw new IllegalArgumentException("入参个数不正确, parameterTypes.length" + parameterTypes.length);
            }

            Object[] actualParameterValues = new Object[parameterTypes.length];
            Method findMethod = null;
            try {
                findMethod = ClassUtils.getMethodByStringType(interfaceName, method, parameterTypes);
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new IllegalArgumentException("未找到接口" + interfaceName + "#" + method );
            }
            Type []methodTypes = findMethod.getGenericParameterTypes();

            if(methodTypes == null || methodTypes.length == 0) {
                throw new IllegalArgumentException("未找到接口" + interfaceName + "#" + method);
            }
            for(int i=0;i<parameterValues.length;i++) {
                if(DynamicParameterWrap.containsDynamicKey(parameterValues[i])) {
                    actualParameterValues[i] = parameterValues[i];
                }else {
                    actualParameterValues[i] = JSON.parseObject(parameterValues[i], methodTypes[i]);
                }
            }
            return new MethodArgument(parameterTypes, actualParameterValues, methodTypes, findMethod.getReturnType());
        }
        return new MethodArgument();
    }



    public <T> T invoke(GenericService genericService, String method, String []parameterTypes, Object []args, Class<T> responseClass) {
        Object result = genericService.$invoke(method, parameterTypes, args);
        T response = null;
        if(result != null) {
            if(responseClass.isAssignableFrom(result.getClass())) {
                response = (T) result;
            }else if(result instanceof Map){
                response = new JSONObject((Map)result).toJavaObject(responseClass);
            }
        }
        return response;
    }
}
