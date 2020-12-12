package com.okeeper.performance.core;

public class MonitorWaterLineCalculator {

	// 累计调用次数
    private long count;
    // 占位数组
    private final double[] SCALE = new double[2400];
    // 统计数组
    private long[] countContainer = new long[2400];
    // 水位线
    private double percentXx;

    /**
     * 构造方法
     *
     * @param waterLine 水位线
     */
    public MonitorWaterLineCalculator(double waterLine) {
        if (waterLine < 0.0 || waterLine > 100.0) {
            throw new IllegalArgumentException("waterLine must be less than 100.0 and more than 0.0");
        } else {
            percentXx = (100.0 - waterLine) / 100;
        }

        // 初始化 SCALE 数组，1 ~ 1000 步长为 1
        for (int i = 0; i < 1000; i++) {
            SCALE[i] = i + 1;
        }
        // 初始化 SCALE 数组，1001 ~ 10000 步长为 10
        for (int i = 1000, j = 10; i < 1900; i++, j += 10) {
            SCALE[i] = 1000 + j;
        }
        // 初始化 SCALE 数组，10001 ~ 60000 步长为 100
        for (int i = 1900, j = 100; i < 2400; i++, j += 100) {
            SCALE[i] = 10000 + j;
        }
    }

    /**
     * 计算方法
     *
     * @param value 待计算值
     */
    public synchronized void accumulate(double value) {
    	// 累加次数
        count++;
        // 找到下标
        int index = positionInValueArray(value);
        countContainer[index]++;
    }

    // 容错，确保通过水位线处理后的数值在 [1, MAX] 范围内
    private long adjust(long input, long max) {
        if (input <= 1) {
            return 1;
        } else if (input >= max) {
            return max;
        } else {
            return input;
        }
    }

    public synchronized double getResult() {
        // 为位置进行容错，该值应该属于 [1,total] 范围
        long percentXxPos = adjust((long) (count * percentXx), count);
        double percentXxValue = Double.MAX_VALUE;
        // 开始遍历每一个元素，从后往前算
        int scanned = 0;
        int length = countContainer.length;

        // 计算结果，或者符合水位线的值，为在步长误差范围内的模糊值
        for (int index = length - 1; index >= 0; index--) {
            // 当前没有值，无论如何也不会成为备选
            if (0.0 == countContainer[index]) {
                continue;
            }

            // 当前有值
            scanned += countContainer[index];
			// 水位线
            if (scanned >= percentXxPos) {
                percentXxValue = SCALE[index];
                break;
            }
        }

        return percentXxValue;
    }

    /**
     * 寻找待处理数值在数组中的位置
     *
     * @param val 待处理值
     * @return 数组中的位置
     */
    private int positionInValueArray(double val) {
        int length = SCALE.length;
        // 如果大于最大值或者小于等于最小值
        if (val >= SCALE[length - 1]) {
            return length - 1;
        } else if (val <= SCALE[0]) {
            return 0;
        }
        // 采用二分法计算
        return binarySearch(SCALE, 0, length - 1, val);
    }

    /**
     * 二分查询
     *
     * @param array 待处理数组
     * @param begin 查询数组起始索引
     * @param end   查询数组结束索引
     * @param value 待查询值
     * @return 数组中的位置
     */
    private int binarySearch(double[] array, int begin, int end, double value) {
        int mid = (begin + end) >> 1;
        double midValue = array[mid];
        double halfMidValue = midValue / 2;
        // 判断是否可以命中
        if (value > halfMidValue && value <= midValue) {
            return mid;
        }
        // 没法命中,则根据大小来定
        if (value <= halfMidValue) {
            // 处理边界条件
            if (mid - 1 < 0) {
                return 0;
            }
            return binarySearch(array, begin, mid - 1, value);
        } else {
            return binarySearch(array, mid + 1, end, value);
        }
    }
}