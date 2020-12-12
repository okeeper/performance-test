package com.okeeper.performance.utils;


import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author shaoyf
 */
@UtilityClass
public class URLUtils {

    private final static String URL_SEPARATOR = ",";


    public String chooseRandomUrl(String url) {
        if (isSingleUrl(url)) {
            return url;
        }
        String[] urls = url.split(URL_SEPARATOR);
        int index = RandomUtils.nextInt(0, urls.length);
        return urls[index];
    }

    private boolean isSingleUrl(String url) {
        return !url.contains(URL_SEPARATOR);
    }


}
