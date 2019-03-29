package com.algorithm;

import com.algorithm.dispatch.DispatchUtils;

import java.math.BigDecimal;

public class SingleTest {
    public static void main(String[] args) {
        int n = 10;
        Long[] array = new Long[n];
        for (int i = 0; i < n; i++) {
            array[i] = 1L << i;
        }
        try {
            BigDecimal result = DispatchUtils.samplingVerify(array, 10, 63, 10000);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
