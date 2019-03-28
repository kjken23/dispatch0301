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
//    private Long[] array;
    private final ArrayBlockingQueue<List<Integer[]>> queue;
    private final CountDownLatch countDownLatch;
//    private int totalCount = 0;
    private Map<Integer, Integer> map;
    private volatile boolean flag;

    public VerifyConsumer(int num, int n, int t, BigDecimal rate, Map<List<Integer[]>, VerifyResult> combineResult, ArrayBlockingQueue<List<Integer[]>> queue, CountDownLatch countDownLatch, boolean flag) {
        this.num = num;
        this.n = n;
        this.t = t;
        this.rate = rate;
        this.combineResult = combineResult;
//        array = new Long[n];
        this.queue = queue;
        this.countDownLatch = countDownLatch;
        this.flag = flag;
    }

    private boolean judgeSingleNode(Long[] arrayList, int i) {
        long mask = (long)((1 << t) - 1);
        long others = 0L;
        for (int j = 0; j < n; j++) {
            if (j == i) {
                continue;
            }
            others = others | arrayList[j];
        }
//        totalCount++;
        return ((arrayList[i] & mask) & (~others & mask)) > 0L;
    }

    private void judge(Long[] arrayList, Map<Integer, Integer> map) throws Exception {
        if (arrayList.length == 0) {
            throw new Exception("数组长度不能为0");
        }
        for (int i = 0; i < n; i++) {
            if(judgeSingleNode(arrayList, i)) {
                Integer count = map.get(i);
                map.replace(i, ++count);
            }
        }
    }

//    private void verify(Long[] arrayList, int n) throws Exception {
//        if (arrayList.length == 0) {
//            throw new Exception("数组长度不能为0");
//        }
//        if (n == 0) {
//            array = arrayList.clone();
//        }
//        if (n != arrayList.length - 1) {
//            for (int i = 0; i < t + 1; i++) {
//                if (i > 0) {
//                    arrayList[n] = DispatchUtils.rotateRight(arrayList[n], 1, t);
//                }
//                if (i == t) {
//                    arrayList = array;
//                    break;
//                }
//                verify(arrayList, n + 1);
//            }
//        } else {
//            for (int i = 0; i < t + 1; i++) {
//                if (i > 0) {
//                    arrayList[n] = DispatchUtils.rotateRight(arrayList[n], 1, t);
//                }
//                if (i == t) {
//                    arrayList = array;
//                    break;
//                }
//                judge(arrayList, map);
//            }
//        }
//    }

    private BigDecimal samplingVerify(Long[] arrayList, int t,int samplingNum) throws Exception {
        if (arrayList.length == 0) {
            throw new Exception("数组长度不能为0");
        }
        Random random;
        Long[] temp = arrayList.clone();
        map = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            map.put(i, 0);
        }
        for (int i = 0; i < samplingNum; i++) {
            for (int j = 0; j < arrayList.length; j++) {
                random  = new Random();
                int offset = random.nextInt(t);
                temp[j] = DispatchUtils.rotateRight(temp[j], offset, t);
            }
            judge(temp, map);
        }
        int count = 0;
        for(Map.Entry<Integer, Integer> entry : map.entrySet()) {
            count += entry.getValue();
        }
        return new BigDecimal((double) count / (double) (4 * samplingNum)).setScale(6, BigDecimal.ROUND_HALF_EVEN);
    }

//    private BigDecimal formatAndVerify(List<Integer[]> list) {
//        Long[] matrix = DispatchUtils.formatMatrix(n, t , list);
//        try {
//            verify(matrix, 0);
//            int count = 0;
//            for(Map.Entry<Integer, Integer> entry : map.entrySet()) {
//                count += entry.getValue();
//            }
//            return new BigDecimal((double) count / (double) totalCount).setScale(6, BigDecimal.ROUND_HALF_EVEN);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new BigDecimal(0);
//        }
//    }

    private BigDecimal formatAndVerifySampling(List<Integer[]> list, int samplingNum) {
        Long[] matrix = DispatchUtils.formatMatrix(n, t , list);
        try {
           return samplingVerify(matrix, t, samplingNum);
        } catch (Exception e) {
            e.printStackTrace();
            return new BigDecimal(0);
        }
    }

    @Override
    public void run() {
        double total = Math.pow(t, n) * 0.01;
        int samplingNum = total > 50000 ? 50000 :  Math.max((int) (total / 1000), 1) * 1000;
        System.out.println("抽样数：" + samplingNum);
        while(flag) {
//            totalCount = 0;
            List<Integer[]> arr = null;
            map = new HashMap<>(n);
            for (int i = 0; i < n; i++) {
                map.put(i, 0);
            }
            try {
                if (flag) {
                    arr = queue.poll(30, TimeUnit.SECONDS);
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(arr != null) {
//                BigDecimal reliability = formatAndVerify(arr);
                BigDecimal samplingReliability = formatAndVerifySampling(arr, samplingNum);
                if (samplingReliability.compareTo(rate) > 0) {
                    BigDecimal repetitiveRate = DispatchUtils.calculateRepetitiveRate(arr, n);
                    VerifyResult verifyResult = new VerifyResult(samplingReliability, repetitiveRate);
                    combineResult.put(arr, verifyResult);
                    System.out.println("验证线程" + num + "-调度方案：");
                    for (Integer[] a : arr) {
                        System.out.println(Arrays.toString(a));
                    }
                    System.out.println("验证通过！");
                    System.out.println("抽样可靠率：" + samplingReliability);
                    System.out.println("重复率：" + repetitiveRate);
                }
            } else {
                break;
            }
        }
        countDownLatch.countDown();
        System.out.println("---------------验证线程" + num + "结束--------------");
    }
}
