package java7;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * Created by IntelliJ IDEA.
 * User: chris
 * Date: 11/07/15
 */
public class Concurrent {
    private static final int THRESHOLD = 10;

    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();
        final int len = 100_000;
        List<Integer> list = createRandomList(len);

        long start1 = System.nanoTime();
        List<Integer> sorted1 = new MergeSort(list).compute();
        long finish1 = System.nanoTime();
        System.out.println("Sorted " + len + " ints serially in " + (finish1 - start1) / 1_000_000 + "ms");

        System.out.println("Sorting using fork join");
        long start2 = System.nanoTime();
        List<Integer> sorted2 = pool.invoke(new MergeSortTask(list));
        long finish2 = System.nanoTime();
        System.out.println("Sorted " + len + " ints using fork join pool in " + (finish2 - start2) / 1_000_000 + "ms (parallelism=" + pool.getParallelism() + ")");

        System.out.println("Fork-join seems dead slow on mergesort with 2 cores.");
        System.out.println("Let's try in-place quicksort on an array...");

        final int arrayLen = 10_000;
        int[] array = createRandomArray(arrayLen);
        int[] array2 = Arrays.copyOf(array, arrayLen);

        long start3 = System.nanoTime();
        new QuickSort(array, 0, arrayLen - 1).compute();
        long finish3 = System.nanoTime();
        System.out.println("Sorted " + arrayLen + " ints serially in " + (finish3 - start3) / 1_000_000 + "ms");

        long start4 = System.nanoTime();
        pool.invoke(new QuickSortTask(array2, 0, arrayLen - 1));
        long finish4 = System.nanoTime();
        System.out.println("Sorted " + arrayLen + " ints using fork join in " + (finish4 - start4) / 1_000_000 + "ms");

        System.out.println("Conclusion: fork-join is useless, at least on only 2 cores?");

    }

    private static List<Integer> createRandomList(int len) {
        Random rnd = new Random();
        List<Integer> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            list.add(rnd.nextInt());
        }
        return list;
    }

    private static int[] createRandomArray(int len) {
        Random rnd = new Random();
        int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            array[i] = rnd.nextInt();
        }
        return array;
    }

    private static List<Integer> merge(List<Integer> left, List<Integer> right) {
        List<Integer> result = new ArrayList<>(left.size() + right.size());
        int leftI = 0, rightI = 0;
        while (leftI < left.size() || rightI < right.size()) {
            if (leftI < left.size() && rightI < right.size()) {
                if (left.get(leftI) <= right.get(rightI)) {
                    result.add(left.get(leftI++));
                } else {
                    result.add(right.get(rightI++));
                }
            } else if (leftI < left.size()) {
                result.add(leftI++);
            } else if (rightI < right.size()) {
                result.add(rightI++);
            }
        }
        return result;
    }

    private static int partition(int[] arr, int left, int right, int pivotIndex) {
        final int pivotVal = arr[pivotIndex];

        // swap arr[pivotIndex] and arr[right]
        arr[pivotIndex] = arr[right];
        arr[right] = pivotVal;

        int index = left;
        for (int i = left; i < right; i++) {
            int val;
            if ((val = arr[i]) < pivotVal) {
                // swap arr[i] and arr[index]
                arr[i] = arr[index];
                arr[index] = val;
                index++;
            }
        }
        // swap arr[index] and arr[right]
        int val = arr[index];
        arr[index] = arr[right];
        arr[right] = val;

        return index;
    }

    static class MergeSort {
        private final List<Integer> list;

        private MergeSort(List<Integer> input) {
            this.list = new ArrayList<>(input);
        }

        public List<Integer> compute() {
            if (list.size() < THRESHOLD) {
                Collections.sort(list); // insertion sort
                return list;
            } else {
                int middle = list.size() / 2;
                List<Integer> leftSorted = new MergeSort(list.subList(0, middle)).compute();
                List<Integer> rightSorted = new MergeSort(list.subList(middle, list.size())).compute();
                return merge(leftSorted, rightSorted);
            }
        }
    }

    static class MergeSortTask extends RecursiveTask<List<Integer>> {
        private final List<Integer> list;

        private MergeSortTask(List<Integer> input) {
            this.list = new ArrayList<>(input);
        }

        public List<Integer> compute() {
            if (list.size() < THRESHOLD) {
                Collections.sort(list); // insertion sort
                return list;
            } else {
                int middle = list.size() / 2;
                MergeSortTask leftTask = new MergeSortTask(list.subList(0, middle));
                leftTask.fork();
                MergeSortTask rightTask = new MergeSortTask(list.subList(middle, list.size()));
                rightTask.fork();
                return merge(leftTask.join(), rightTask.join());
            }
        }
    }

    static class QuickSort {
        private final int[] arr;
        private final int left, right;

        private QuickSort(int[] arr, int left, int right) {
            this.arr = arr;
            this.left = left;
            this.right = right;
        }

        public void compute() {
            if (right > left) {
                int pivotIndex = (left + right) / 2; // ignore int overflow problem
                int newPivotIndex = partition(arr, left, right, pivotIndex);
                new QuickSort(arr, left, newPivotIndex - 1).compute();
                new QuickSort(arr, newPivotIndex + 1, right).compute();
            }
        }
    }

    static class QuickSortTask extends RecursiveAction {
        private final int[] arr;
        private final int left, right;

        private QuickSortTask(int[] arr, int left, int right) {
            this.arr = arr;
            this.left = left;
            this.right = right;
        }

        public void compute() {
            if (right > left) {
                int pivotIndex = (left + right) / 2; // ignore int overflow problem
                int newPivotIndex = partition(arr, left, right, pivotIndex);
                QuickSortTask leftTask = new QuickSortTask(arr, left, newPivotIndex - 1);
                leftTask.fork();
                QuickSortTask rightTask = new QuickSortTask(arr, newPivotIndex + 1, right);
                rightTask.fork();

                leftTask.join();
                rightTask.join();
            }
        }
    }
}
