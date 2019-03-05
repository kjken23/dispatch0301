package com.algorithm.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * 组合模式-生产者
 * @author kj
 */
public class ArrayProducer implements Runnable {
    private List<Integer> tmpArr = new ArrayList<>();
    private int index;
    private int k;
    private List<Integer> arr;
    private final ArrayBlockingQueue<List<Integer[]>> queue;
    private final CountDownLatch countDownLatch;
    private Map<Integer, Integer[]> resultMap;

    public ArrayProducer(int index, int k, List<Integer> arr, ArrayBlockingQueue<List<Integer[]>> queue, CountDownLatch countDownLatch, Map<Integer, Integer[]> resultMap) {
        this.index = index;
        this.k = k;
        this.arr = arr;
        this.queue = queue;
        this.countDownLatch = countDownLatch;
        this.resultMap = resultMap;
    }

    private void combine(int index, int k, List<Integer> arr) {
        if (k == 1) {
            for (int i = index; i < arr.size(); i++) {
                tmpArr.add(arr.get(i));
                List<Integer[]> tmp = new ArrayList<>();
                for (Integer integer : tmpArr) {
                    tmp.add(resultMap.get(integer));
                }
                try {
                    queue.put(tmp);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tmpArr.remove(arr.get(i));
            }
        } else if (k > 1) {
            for (int i = index; i <= arr.size() - k; i++) {
                tmpArr.add(arr.get(i));
                combine(i + 1, k - 1, arr);
                tmpArr.remove(arr.get(i));
            }
        }
    }

    @Override
    public void run() {
        try{
            combine(index, k, arr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            countDownLatch.countDown();
            System.out.println("------------生产者结束---------------");
        }
    }
}
