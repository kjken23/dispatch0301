package com.algorithm;

import com.algorithm.dispatch.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

/**
 * 主程序
 *
 * @author kj
 */
public class App {
    private static final int THREAD_NUM = 10;
    private static List<Integer> tmpArr2 = new ArrayList<>();
    private static Map<Integer, HashSet<Integer>> frontward = new HashMap<>();
    private static Map<Integer, HashSet<Integer>> backward = new HashMap<>();
    private static Map<List<Integer[]>, VerifyResult> combineResult = new ConcurrentHashMap<>();

    private static boolean ruleOutNoRepeat(Integer[] array, int n) {
        Set<Integer> repeat = new HashSet<>(Arrays.asList(array));
        return repeat.size() != n;
    }


    public static void main(String[] args) {
        // 输入n和T
        Scanner sc = new Scanner(System.in);
        System.out.print("请输入n：");
        int n = sc.nextInt();
        System.out.print("请输入T：");
        int t = sc.nextInt();
        System.out.print("请输入可靠率：");
        BigDecimal reliability = sc.nextBigDecimal();
        // 启发式参数
        BigDecimal baseLine = reliability.multiply(new BigDecimal(3.92)).subtract(new BigDecimal(3)).setScale(6, BigDecimal.ROUND_HALF_EVEN);
        System.out.println("--------------------------");
        if (n > t) {
            System.out.println("请检查输入的n和T的值！");
            return;
        }
        if (reliability.compareTo(BigDecimal.ONE) >= 0) {
            System.out.println("可靠度应小于等于1！");
            return;
        }

        Date start = new Date();
        // 使用多线程计算整数分割问题
        // 此处有一个疑问，多线程是否有必要，还没有做过测试
        List<Integer[]> pattenList = new ArrayList<>();
        ExecutorService findPattenExecutorService = new ThreadPoolExecutor(n, n, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        List<Future<Map<Integer, List<Integer[]>>>> futureList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            FindPatten findPatten = new FindPatten(n - i, t - i);
            futureList.add(findPattenExecutorService.submit(findPatten));
        }
        for (Future<Map<Integer, List<Integer[]>>> future : futureList) {
            try {
                for (Map.Entry<Integer, List<Integer[]>> entry : future.get().entrySet()) {
                    // 对于少于n的结果补0
                    if (entry.getKey() != n) {
                        for (Integer[] ca : entry.getValue()) {
                            Integer[] newca = new Integer[n];
                            for (int i = 0; i < n - entry.getKey(); i++) {
                                newca[i] = 0;
                            }
                            System.arraycopy(ca, 0, newca, n - entry.getKey(), ca.length);
                            pattenList.add(newca);
                        }
                    } else {
                        pattenList.addAll(entry.getValue());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        findPattenExecutorService.shutdown();

        System.out.println("----------整数划分完毕------------");

        //对计算出的整数分割结果进行全排列
        Map<Integer, Integer[]> allSortResult = new HashMap<>();
        for (Integer[] sort : pattenList) {
            DispatchUtils.allSort(sort, 0, sort.length - 1, allSortResult);
        }

        System.out.println("----------划分结果全排列完毕------------");

        //对全排列结果进行归一化
        List<Integer[]> normalizedList = new ArrayList<>();
        for (Map.Entry<Integer, Integer[]> entry : allSortResult.entrySet()) {
            Integer[] normalized = NormalizePatten.normalize(entry.getValue());
            normalizedList.add(normalized);
        }

        System.out.println("----------归一化完毕------------");

        //去除归一化结果中的冗余部分
        Map<Integer, Integer[]> normalizedResult = NormalizePatten.removeRedundancy(normalizedList);

        //创建间隔索引
        Collection<Integer[]> resultCollection = normalizedResult.values();
        Map<Integer, Integer[]> resultMap = new HashMap<>(resultCollection.size());
        List<Integer> resultList = new ArrayList<>();

        int index = 0;
        for (Integer[] array : resultCollection) {
            if (ruleOutNoRepeat(array, n)) {
                resultMap.put(index, array);
                resultList.add(index);
                HashSet<Integer> set = new HashSet<>(Arrays.asList(array));
                Integer[] tempArray = new Integer[array.length];
                System.arraycopy(array, 0, tempArray, 0, array.length);
                for (int i = 2; i < n; i++) {
                    for (int j = 0; j < tempArray.length; j++) {
                        tempArray = DispatchUtils.moveArrayElement(tempArray, 1);
                        HashSet<Integer> sum = DispatchUtils.combine2(0, i, Arrays.asList(tempArray), new HashSet<>(), i - 1, tmpArr2);
                        set.addAll(sum);
                    }
                }
                frontward.put(index, set);
                for (Integer d : set) {
                    HashSet<Integer> temp = backward.get(d) == null ? new HashSet<>() : backward.get(d);
                    temp.add(index);
                    backward.put(d, temp);
                }
                index++;
            }
        }

        //组合模式
        ArrayBlockingQueue<List<Integer[]>> queue = new ArrayBlockingQueue<>(1000);
        final CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM + 1);
        ArrayProducer producer = new ArrayProducer(0, n, resultList, queue, countDownLatch, resultMap, frontward, baseLine);

        ExecutorService producerExecutorService = new ThreadPoolExecutor(1, 1, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        ExecutorService verifyExecutorService = new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<>());



        System.out.println("-----------生产者启动-----------------");
        producerExecutorService.submit(producer);
        System.out.println("-----------消费者启动-----------------");
        for (int i = 0; i < THREAD_NUM; i++) {
            verifyExecutorService.submit(new VerifyConsumer(i, n, t, reliability, combineResult, queue, countDownLatch));
        }

        try {
            countDownLatch.await();
            producerExecutorService.shutdown();
            verifyExecutorService.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("----------模式验证完毕------------");

        if (combineResult.size() > 0) {
            DispatchUtils.writeExcel(combineResult);
            System.out.println("T = " + t + "时： 共" + combineResult.size() + "种调度方案");
        } else {
            System.out.println("T = " + t + "时：");
            System.out.println("该情况下没有符合条件的调度方案");
        }
        Date end = new Date();
        long total = end.getTime() - start.getTime();
        if (total < 1000) {
            System.out.println("共耗时" + total + "毫秒");
        } else {
            System.out.println("共耗时" + total / 1000 + "秒");
        }
    }
}
