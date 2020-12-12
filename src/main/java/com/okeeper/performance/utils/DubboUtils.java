package com.okeeper.performance.utils;

import com.okeeper.performance.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DubboUtils {

    private static ConcurrentHashMap<String, Object> cachedService = new ConcurrentHashMap<>();
    static {
        addDestroyHook();
    }

    public static <T> T getBean(Class<T> clazz) {
        String interfaceName = clazz.getName();
        T genericService = (T) cachedService.get(interfaceName);
        if(genericService == null) {
            ReferenceConfig<T> reference = getReferenceConfig(interfaceName);
            cachedService.putIfAbsent(interfaceName, ReferenceConfigCache.getCache().get(reference));
            log.info("init bean and cache. {}", interfaceName);
            return (T) cachedService.get(interfaceName);
        }else {
            log.info("getBean from cache. {}", interfaceName);
            return genericService;
        }
    }

    private static void addDestroyHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> destroy()));
    }


    public static void destroy() {
        for(String interfaceName : cachedService.keySet()) {
            DubboUtils.destroy(interfaceName);
        }
        log.info("All interface hook shutdown.");
    }

    public static void destroy(Class clazz) {
       destroy(clazz.getName());
    }

    public static void destroy(String interfaceName) {
        cachedService.remove(interfaceName);
        ReferenceConfigCache.getCache().destroy(getReferenceConfig(interfaceName));
        log.info("destroy {} success.", interfaceName);
    }

    private static<T> ReferenceConfig<T> getReferenceConfig(String interfaceName) {
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setApplication(new ApplicationConfig(Constants.APP_NAME));
        reference.setInterface(interfaceName);
        return reference;
    }
}
