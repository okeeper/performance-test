package com.okeeper.performance.utils;

import com.okeeper.performance.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class ClassLoaderUtils {

    public static Class loadClassFromExt(String className) {
        try {
            URLClassLoader classLoader = new URLClassLoader(loadAllJarURLs(Constants.EXT_JAR_PATH), Thread.currentThread()
                    .getContextClassLoader());
            return classLoader.loadClass(className);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("load error for " + className);
        }
    }

    public static URL[] loadAllJarURLs(String path) {
        List<URL> allJars = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(new File(Constants.EXT_JAR_PATH), null, false);
        files.stream().forEach(file -> {
            if(file.getName().lastIndexOf(".jar") != -1) {
                try {
                    allJars.add(new URL(file.getPath()));
                } catch (MalformedURLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        return allJars.toArray(new URL[0]);
    }

}
