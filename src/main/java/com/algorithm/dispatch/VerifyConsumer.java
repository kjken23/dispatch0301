package com.algorithm.dispatch;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 验证
 *
 * @author kj
 */
public class VerifyConsumer implements Runnable {
    private int num;
    private int n;
    private int t;
    private BigDecimal rate;
    private Map<List<Integer[]>, VerifyResult> combineResult;
    private Long[] array;
    private final ArrayBlockingQueue<List<Integer[]>> queue;
    private final CountDownLatch countDownLatch;
    private int correctNum = 0;

    public VerifyConsumer(int num, int n, int t, BigDecimal rate, Map<List<Integer[]>, VerifyResult> combineResult, ArrayBlockingQueue<List<Integer[]>> queue, CountDownLatch countDownLatch) {
        this.num = num;
        this.n = n;
        this.t = t;
        this.rate = rate;
        this.combineResult = combineResult;
        array = new Long[n];
        this.queue = queue;
        this.countDownLatch = countDownLatch;
    }

    private boolean judge(Long[] arrayList) {
        if (arrayList.length == 0) {
            return false;
        }
        long mask = (long)((1 << t) - 1);
        for (int i = 0; i < n; i++) {
            long others = 0L;
            for (int j = 0; j < n; j++) {
                if (j == i) {
                    continue;
                }
                others = others | arrayList[j];
            }
            if(((arrayList[i] & mask) & (~others & mask)) <= 0L) {
                return false;
            }
        }
        return true;
    }

    private boolean verify(Long[] arrayList, int n) {
        if (arrayList.length == 0) {
            return false;
        }
        if (n == 0) {
            array = arrayList.clone();
        }
        boolean flag = true;
        if (n != arrayList.length - 1) {
            for (int i = 0; i < t + 1; i++) {
                if (i > 0) {
                    arrayList[n] = DispatchUtils.rotateRight(arrayList[n], 1, t);
                }
                if (i == t) {
                    arrayList = array;
                    break;
                }
                verify(arrayList, n + 1);
            }
        } else {
            for (int i = 0; i < t + 1; i++) {
                if (i > 0) {
                    arrayList[n] = DispatchUtils.rotateRight(arrayList[n], 1, t);
                }
                if (i == t) {
                    arrayList = array;
                    break;
                }
                flag = judge(arrayList);
                if (flag) {
                    correctNum++;
                }
            }
        }
        return true;
    }

    private BigDecimal formatAndVerify(List<Integer[]> list) {
        char[][] tempMartix = DispatchUtils.initArray(n, t);
        for (int i = 0, len1 = list.size(); i < len1; i++) {
            int pos = 1;
            for (Integer integer : list.get(i)) {
                pos += integer;
                tempMartix[i][pos % t] = '1';
                pos++;
            }
        }
        Long[] martix = new Long[n];
        for (int i = 0; i < tempMartix.length; i++) {
            martix[i] = Long.valueOf(String.valueOf(tempMartix[i]), 2);
        }
        verify(martix, 0);
        return new BigDecimal(correctNum / (Math.pow(t,n))).setScale(6, BigDecimal.ROUND_HALF_EVEN);
    }

    @Override
    public void run() {
        while(true) {
            correctNum = 0;
            List<Integer[]> arr = null;
            try {
                arr = queue.poll(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(arr != null) {
                BigDecimal reliability = formatAndVerify(arr);
                if (reliability.compareTo(rate) > 0) {
                    BigDecimal repetitiveRate = DispatchUtils.calculateRepetitiveRate(arr, n);
                    VerifyResult verifyResult = new VerifyResult(reliability, repetitiveRate);
                    combineResult.put(arr, verifyResult);
                    System.out.println("验证线程" + num + "-调度方案：");
                    for (Integer[] a : arr) {
                        System.out.println(Arrays.toString(a));
                    }
                    System.out.println("验证通过！");
                    System.out.println("可靠率：" + reliability);
                    System.out.println("重复率：" + repetitiveRate);
                }
            } else {
                countDownLatch.countDown();
                System.out.println("---------------验证线程" + num + "结束--------------");
                break;
            }
        }
    }
}
