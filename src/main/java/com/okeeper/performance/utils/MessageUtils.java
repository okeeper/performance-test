package com.okeeper.performance.utils;

import org.slf4j.helpers.MessageFormatter;

/**
 * @author zhangyue1
 */
public class MessageUtils {

    /**
     * 支持占位符
     *  MessageUtils.format("this is {}, and my name is {}.", "lilei", "hanmeimei");
     * 文本格式化
     * @param message
     * @param params
     * @return
     */
    public static String format(String message, Object ...params) {
        return MessageFormatter.arrayFormat(message, params).getMessage();
    }
}
