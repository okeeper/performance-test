/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okeeper.performance.utils;

import com.okeeper.performance.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ClassUtils
 */
@Slf4j
public class ClassUtils {

    public static Method getMethodByStringType(String interfaceName,
                                                        String methodName, String[] paramStringTypes) throws ClassNotFoundException {
        // 创建类
        //Class<?> class1 = Class.forName(interfaceName);
        Class class1 = loadClassFromExtJars(interfaceName);
        // 获取所有的公共的方法
        Method[] methods = class1.getMethods();
        for (Method method : methods) {
            Type[] parameterTypes = method.getGenericParameterTypes();
            if (method.getName().equals(methodName) && parameterTypes.length == paramStringTypes.length) {
                return method;
            }
        }
        return null;

    }


    public static Class loadClassFromExtJars(String className) throws ClassNotFoundException {
            URLClassLoader classLoader = new URLClassLoader(loadAllJarURLs(Constants.EXT_JAR_PATH), Thread.currentThread()
                    .getContextClassLoader());
            return classLoader.loadClass(className);
    }

    public static URL[] loadAllJarURLs(String path) {
        List<URL> allJars = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(new File(path), null, false);
        files.stream().forEach(file -> {
            if (file.getName().lastIndexOf(".jar") != -1) {
                try {
                    String url = "file://" + file.getPath();
                    log.info("load file {} success.", url);
                    allJars.add(new URL(url));
                } catch (MalformedURLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        return allJars.toArray(new URL[0]);
    }

}
