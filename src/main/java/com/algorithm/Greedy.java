package com.algorithm;

import com.algorithm.dispatch.DispatchUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Greedy {
    private static final int NUM = 1000;
    private static final int N = 10;
    private static final int T = 63;
    private static final int TEMP_N = 7;
    private static final int TEMP_T = 43;
    private static final List<Integer[]> LIST = new ArrayList<>(Arrays.asList(new Integer[]{0, 14, 11, 1, 1, 1, 8}, new Integer[]{1, 3, 4, 5, 3, 8, 12},
            new Integer[]{1, 2, 18, 4, 4, 2, 5}, new Integer[]{0, 1, 4, 4, 14, 2, 11}, new Integer[]{0, 2, 6, 2, 6, 10, 10}, new Integer[]{0, 4, 12, 6, 3, 4, 7},
            new Integer[]{0, 1, 13, 7, 1, 6, 8}));

    private static char[][] initArray(int n, int t, int num) {
        char[][] arrayList = new char[n][t];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < t; j++) {
                if (j == 0 && i < num) {
                    arrayList[i][j] = '1';
                } else {
                    arrayList[i][j] = '0';
                }
            }
        }
        return arrayList;
    }

    public static void main(String[] args) {
        char[][] tempMatrix = initArray(N, T, LIST.size());
        for (int i = 0, len1 = LIST.size(); i < len1; i++) {
            int pos = 1;
            for (Integer integer : LIST.get(i)) {
                pos += integer;
                tempMatrix[i][pos % T] = '1';
                pos++;
            }
        }
        Long[] matrix = new Long[N];
        for (int i = 0; i < tempMatrix.length; i++) {
            matrix[i] = Long.valueOf(String.valueOf(tempMatrix[i]), 2);
        }
        double total = Math.pow(T, N) * 0.01;
        int samplingNum = total > 100000 ? 100000 :  Math.max((int) (total / 1000), 1) * 1000;
        try {
            BigDecimal baseLine = DispatchUtils.samplingVerify(matrix, N, T, samplingNum);
            System.out.println(baseLine);

            int count = 0;
            Random xRandom;
            Random yRandom;
            while (count < NUM) {
                xRandom = new Random();
                yRandom = new Random();
                int x = xRandom.nextInt(N - 1) % (N - TEMP_N ) + TEMP_N;
                int y = yRandom.nextInt(T - 1) % (T - TEMP_T ) + TEMP_T;
                if(tempMatrix[x][y] == '0') {
                    tempMatrix[x][y] = '1';

                } else {
                    continue;
                }
                Long[] innerMatrix = new Long[N];
                for (int i = 0; i < tempMatrix.length; i++) {
                    innerMatrix[i] = Long.valueOf(String.valueOf(tempMatrix[i]), 2);
                }
                BigDecimal result = DispatchUtils.samplingVerify(innerMatrix, N, T, samplingNum);
                if(result.compareTo(baseLine) > 0) {
                    System.out.println(result);
                    baseLine = result;
                } else {
                    tempMatrix[x][y] = '0';
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
