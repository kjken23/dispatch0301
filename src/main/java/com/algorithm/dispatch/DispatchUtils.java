package com.algorithm.dispatch;

import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * 公共工具类
 *
 * @author kj
 */
public class DispatchUtils {
    public static Integer[] moveArrayElement(Integer[] array, int k) {
        if (k == 0) {
            return array;
        }
        int length = array.length;
        // 右移newk + n * length个位置，和右移newk个位置效果是一样的
        int newk = k % length;
        Integer[] newArray = new Integer[length];
        // 重复length次把元素从旧位置移到新位置
        for (int i = 0; i < length; i++) {
            // 求出元素新的位置
            int newPosition = (i + newk) % length;
            newArray[newPosition] = array[i];
        }
        return newArray;
    }

    public static void deepCopy(Integer[][] copyFrom, Integer[][] copyTo) {
        for (int i = 0; i < copyFrom.length; i++) {
            copyTo[i] = copyFrom[i].clone();
        }
    }

    public static char[][] initArray(int n, int t) {
        char[][] arrayList = new char[n][t];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < t; j++) {
                if (j == 0) {
                    arrayList[i][j] = '1';
                } else {
                    arrayList[i][j] = '0';
                }
            }
        }
        return arrayList;
    }

    public static void allSort(Integer[] array, int begin, int end, Map<Integer, Integer[]> result) {
        if (begin == end) {
            Integer[] temp = array.clone();
            int hash = Arrays.hashCode(temp);
            if (!result.containsKey(hash)) {
                result.put(hash, temp);
            }
            return;
        }
        //把子数组的第一个元素依次和第二个、第三个元素交换位置
        for (int i = begin; i <= end; i++) {
            swap(array, begin, i);
            allSort(array, begin + 1, end, result);
            //交换回来
            swap(array, begin, i);
        }
    }

    private static void swap(Integer[] array, int a, int b) {
        int tem = array[a];
        array[a] = array[b];
        array[b] = tem;
    }

    public static int getIndex(Integer[] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return i;
            }
        }
        return -1;
    }

    @Deprecated
    public static BigInteger rotateRight(BigInteger i, int distance, int t) {
        BigInteger right = i.shiftRight(distance);
        BigInteger left = i.shiftLeft(t - distance);
        BigInteger mask = BigInteger.valueOf((2 << t) - 1);
        return right.or(left).and(mask);
    }

    public static Long rotateRight(long i, int distance, int t) {
        long right = i >> distance;
        long left = i << (t - distance);
        long mask = (1L << t) - 1;
        return (right | left) & mask;
    }

    public static HashSet<Integer> combine2(int index, int k, List<Integer> arr, HashSet<Integer> set, int n, List<Integer> tmpArr2) {
        if (k == 1) {
            for (int i = index; i < arr.size(); i++) {
                if (i == index) {
                    tmpArr2.add(arr.get(i));
                    int sum = 0;
                    for (Integer d : tmpArr2) {
                        sum += d;
                    }
                    set.add(sum + n);
                    tmpArr2.remove(arr.get(i));
                }
            }
        } else if (k > 1) {
            for (int i = index; i <= arr.size() - k; i++) {
                if (i == index) {
                    tmpArr2.add(arr.get(i));
                    combine2(i + 1, k - 1, arr, set, n, tmpArr2);
                    tmpArr2.remove(arr.get(i));
                }
            }
        }
        return set;
    }

    public static void calculateSet(Integer[] a, Set<Integer> set, int n, List<Integer> tmpArr) {
        Integer[] tempArray = new Integer[a.length];
        System.arraycopy(a, 0, tempArray, 0, a.length);
        for (int i = 2; i < n; i++) {
            for (int j = 0; j < tempArray.length; j++) {
                tempArray = DispatchUtils.moveArrayElement(tempArray, 1);
                HashSet<Integer> sum = DispatchUtils.combine2(0, i, Arrays.asList(tempArray), new HashSet<>(), i - 1, tmpArr);
                set.addAll(sum);
            }
        }
    }

    public static BigDecimal calculateRepetitiveRate(List<Integer[]> arr, int n) {
        HashSet<Integer> all = new HashSet<>();
        List<Integer> tmpArr = new ArrayList<>();
        int setCount = 0;
        for (Integer[] a : arr) {
            HashSet<Integer> set = new HashSet<>(Arrays.asList(a));
            calculateSet(a, set, n, tmpArr);
            setCount += set.size();
            all.addAll(set);
        }
        return new BigDecimal((double) all.size() / (double) setCount).setScale(6, BigDecimal.ROUND_HALF_EVEN);
    }

    public static BigDecimal calculateRepetitiveRate(List<Integer> arr, Map<Integer, HashSet<Integer>> frontward) {
        HashSet<Integer> all = new HashSet<>();
        int setCount = 0;
        for (Integer a : arr) {
            HashSet<Integer> set = frontward.get(a);
            setCount += set.size();
            all.addAll(set);
        }
        return new BigDecimal((double) all.size() / (double) setCount).setScale(6, BigDecimal.ROUND_HALF_EVEN);
    }

    public static void writeExcel(Map<List<Integer[]>, VerifyResult> map) {
        Date now = new Date();
        SXSSFWorkbook wb = new SXSSFWorkbook();
        SXSSFSheet sheet = wb.createSheet(String.valueOf(now.getTime()));
        sheet.trackAllColumnsForAutoSizing();

        int currentRowNum = 0;
        int currentColNum = 0;
        SXSSFRow row = sheet.createRow(currentRowNum);

        SXSSFCell cell = row.createCell(currentColNum);
        cell.setCellValue("可靠率");
        currentColNum++;

        cell = row.createCell(currentColNum);
        cell.setCellValue("间隔不重复率");

        for (Map.Entry<List<Integer[]>, VerifyResult> entry : map.entrySet()) {
            currentRowNum++;
            currentColNum = 0;
            row = sheet.createRow(currentRowNum);

            cell = row.createCell(currentColNum);
            cell.setCellValue(entry.getValue().getReliability().doubleValue());
            currentColNum++;

            cell = row.createCell(currentColNum);
            cell.setCellValue(entry.getValue().getRepetitiveRate().doubleValue());
        }

        try {
            FileOutputStream fout = new FileOutputStream("D:\\workspace\\dispatch0301\\" + now.getTime() + ".xlsx");
            wb.write(fout);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Long[] formatMatrix(int n, int t, List<Integer[]> list) {
        char[][] tempMatrix = initArray(n, t);
        for (int i = 0, len1 = list.size(); i < len1; i++) {
            int pos = 1;
            for (Integer integer : list.get(i)) {
                pos += integer;
                tempMatrix[i][pos % t] = '1';
                pos++;
            }
        }
        Long[] matrix = new Long[n];
        for (int i = 0; i < tempMatrix.length; i++) {
            matrix[i] = Long.valueOf(String.valueOf(tempMatrix[i]), 2);
        }
        return matrix;
    }

    public static BigDecimal samplingVerify(Long[] arrayList, int n, int t, int samplingNum) throws Exception {
        if (arrayList.length == 0) {
            throw new Exception("数组长度不能为0");
        }
        Random random;
        Long[] temp = arrayList.clone();
        Map<Integer, Integer> map = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            map.put(i, 0);
        }
        for (int i = 0; i < samplingNum; i++) {
            random = new Random();
            for (int j = 0; j < arrayList.length; j++) {
                int offset = random.nextInt(t);
                temp[j] = DispatchUtils.rotateRight(temp[j], offset, t);
            }
            judge(temp, map, n, t);
        }
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            count += entry.getValue();
        }
        return new BigDecimal((double) count / (double) (n * samplingNum)).setScale(6, BigDecimal.ROUND_HALF_EVEN);
    }

    private static boolean judgeSingleNode(Long[] arrayList, int i, int n, int t) {
        long mask = (1L << t) - 1;
        long others = 0L;
        for (int j = 0; j < n; j++) {
            if (j == i) {
                continue;
            }
            others = others | arrayList[j];
        }
        return ((arrayList[i] & mask) & (~others & mask)) > 0L;
    }

    private static void judge(Long[] arrayList, Map<Integer, Integer> map, int n, int t) throws Exception {
        if (arrayList.length == 0) {
            throw new Exception("数组长度不能为0");
        }
        for (int i = 0; i < n; i++) {
            if (judgeSingleNode(arrayList, i, n, t)) {
                Integer count = map.get(i);
                map.replace(i, ++count);
            }
        }
    }
}
