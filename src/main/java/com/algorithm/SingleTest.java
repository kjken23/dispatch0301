package com.algorithm;

import com.algorithm.dispatch.DispatchUtils;

import java.math.BigDecimal;

public class SingleTest {
    public static void main(String[] args) {
        Long[] array = new Long[] {3L, 5L, 9L, 17L, 33L};
        try {
            BigDecimal result = DispatchUtils.samplingVerify(array, 5, 10, 10000);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
