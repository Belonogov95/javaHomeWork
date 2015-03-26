package ru.ifmo.ctddev.belonogov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import ru.ifmo.ctddev.belonogov.mapper.ParallelMapperImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterativeParallelism implements ListIP {

    private ParallelMapper mapper;
    
    void run() {
        int[] data = new int[]{7, 2, 5, 2, 5, 3};
        ArrayList<Integer> b = new ArrayList<>();
        for (int aData : data) b.add(aData);
        try {
//            Integer x = maximum(2, b, new Comparator<Integer>() {
//                @Override
//                public int compare(Integer o1, Integer o2) {
//                    return o1.compareTo(o2);
//                }
//            });
            List < Integer > list = map(2, b, x -> x + 9);
            System.out.println(list);
            //System.out.println(concat(2,list));

//            System.out.println("x: " + x);
            //System.out.println("res: " + concat(2, b));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new IterativeParallelism(new ParallelMapperImpl(2, new LinkedList<>())).run();
    }

    public IterativeParallelism() {  }
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper; 
    }

    private static abstract class Worker<R> implements Runnable {
        private R result;
        int left, right;

        public Worker(int left, int right) {
            this.left = left;
            this.right = right;
        }

        public R getResult() {
            return result;
        }

        protected void setResult(R a) {
            result = a;
        }
    }

    <T, R> R parallel(int threads, List<? extends T> list, Function<? super T, ? extends R> action, Function<List<R>, R> combiner) {
        List<R> threadsResult = new ArrayList<>();
        if (mapper == null) {
            int len = list.size();
            threads = Math.min(len, threads);
            int elementsForThread = (len + threads - 1) / threads;
            //System.out.println("el: " + elementsForThread);
            ArrayList<Worker<R>> workerArrayList = new ArrayList<>();
            ArrayList<Thread> threadArrayList = new ArrayList<>();
            for (int i = 0; i < threads; i++)
                workerArrayList.add(new Worker<R>(i * elementsForThread, Math.min(len, (i + 1) * elementsForThread)) {
                    @Override
                    public void run() {
                        ArrayList<R> g = new ArrayList<R>();
                        for (int i = left; i < right; i++)
                            g.add((R) action.apply(list.get(i)));
                        setResult(combiner.apply(g));
                    }
                });
            for (int i = 0; i < threads; i++)
                threadArrayList.add(new Thread(workerArrayList.get(i)));
            for (int i = 0; i < threads; i++)
                threadArrayList.get(i).start();
            for (int i = 0; i < threads; i++)
                try {
                    threadArrayList.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            for (int i = 0; i < threads; i++)
                threadsResult.add(workerArrayList.get(i).getResult());
        }
       else {
            System.err.println(list.getClass());
            try {
                threadsResult = mapper.map(action, list);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return combiner.apply(threadsResult);
    }


    @Override
    public String concat(int threads, List<?> list) throws InterruptedException {
        Function<Object, String> action = x -> x.toString();
        Function<List<String>, String> combiner = listS -> {
            StringBuilder sb = new StringBuilder();
            for (String list1 : listS) sb.append(list1);
            return sb.toString();
        };
        return parallel(threads, list, action, combiner);
    }

    @Override
    public <T> List<T> filter(int threads , List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function < T, List < T >  > action = x ->  {
            ArrayList listR = new ArrayList < T >();
            if (predicate.test(x))
                listR.add(x);
            return listR;
        };
        Function < List < List < T > >, List < T > > combiner = listT ->  {
            ArrayList < T > result = new ArrayList<>();
            for (List < T > x: listT)
                for (T y: x)
                    result.add(y);
            return result;
        };
        return parallel(threads, list, action, combiner);
    }


    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        Function < T, List < U > > action = x -> {
            ArrayList < U > result = new ArrayList<U>();
            result.add(function.apply(x));
            return result;
        };
        Function < List < List < U > >, List < U > > combiner = listT -> {
            ArrayList<U> result = new ArrayList<>();
            for (List<U> x : listT)
                for (U y : x)
                    result.add(y);
            return result;
        };
        return parallel(threads, list, action, combiner);
    };


    @Override
    public <T> T maximum(int threads, List<? extends T> list, final Comparator<? super T> comparator) throws InterruptedException {
        Function<T, T> action = a -> a;
        Function<List<T>, T> combiner = listT -> {
            T result = null;
            for (T aListT : listT) {
                if (aListT == null)
                    continue;
                if (result == null)
                    result = aListT;
                int r = comparator.compare(result, aListT);
                if (r == -1) {
                    result = aListT;
                }
            }
            return result;
        };
        return parallel(threads, list, action, combiner);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> list, final Comparator<? super T> comparator) throws InterruptedException {
        Function<T, T> action = a -> a;
        Function<List<T>, T> combiner = listT -> {
            T result = null;
            for (T aListT : listT) {
                if (aListT == null) continue;
                if (result == null || comparator.compare(result, aListT) == 1)
                    result = aListT;
            }
            return result;
        };
        return parallel(threads, list, action, combiner);
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<T, Boolean> action = predicate::test;
        Function<List<Boolean>, Boolean> combiner = listB -> listB.stream().filter(x -> !x).count() == 0;
        return parallel(threads, list, action, combiner);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<T, Boolean> action = predicate::test;
        Function<List<Boolean>, Boolean> combiner = listB -> listB.stream().filter(x -> x).count() > 0;
        return parallel(threads, list, action, combiner);
    }
}
