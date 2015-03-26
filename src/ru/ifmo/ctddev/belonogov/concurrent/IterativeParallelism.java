package ru.ifmo.ctddev.belonogov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import ru.ifmo.ctddev.belonogov.mapper.ParallelMapperImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterativeParallelism implements ListIP {
    /**
     *
     */

    private ParallelMapper mapper;

    private void run() {
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
            List<Integer> list = map(2, b, x -> x + 9);


            //System.out.println(list);
            //System.out.println(concat(2,list));

//            System.out.println("x: " + x);
            //System.out.println("res: " + concat(2, b));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

//    public static void main(String[] args) {
//        //new IterativeParallelism(new ParallelMapperImpl(2, new LinkedList<>())).run();
//        new IterativeParallelism(new ParallelMapperImpl(2)).run();
//    }

    public IterativeParallelism() {
    }

    /**
     *
     *
     * @param mapper
     */

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

    private <T, R> R parallel(int threads, List<? extends T> list, Function<? super T, ? extends R> action, Function<List<R>, R> combiner) {
        List<R> threadsResult = new ArrayList<>();
        int len = list.size();
        threads = Math.min(len, threads);
        int elementsForThread = (len + threads - 1) / threads;

        if (mapper == null) {
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
        } else {
            ArrayList<List<? extends T>> data = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                int left = Math.min(i * elementsForThread, len);
                int right = Math.min((i + 1) * elementsForThread, len);
                //System.err.println("left right sz " + left + " " + right + " " + list.size());
                data.add(list.subList(left, right));
            }
            Function<List<? extends T>, R> mapperFunc = l -> {
                ArrayList<R> result = new ArrayList<>();
                for (T x : l)
                    result.add(action.apply(x));
                return combiner.apply(result);
            };
            try {
                //System.err.println("data sz: " + data.size());
                threadsResult = mapper.map(mapperFunc, data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return combiner.apply(threadsResult);
    }

    /**
     * Returns concatenated representations (in the sense of {@link Object#toString()} method)
     * of all the elements of the given {@code list}.
     * <p>
     * For intrinsic purposes it uses {@code threads} of {@link java.lang.Thread}
     * objects. Each of them gets its own part of work to perform the whole work in parallel.
     *
     * @param threads number of {@code Threads} in which work should be done.
     * @param list    {@code List} of elements
     * @return concatenated {@code String} representation of elements
     * @throws InterruptedException
     */


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


    /**
     * For the given {@code list} returns another {@code list} which consists
     * of elements that match given {@code predicate}.
     * <p>
     * For intrinsic purposes it uses {@code threads} of {@link java.lang.Thread}
     * objects. Each of them gets its own part of work to
     * perform the whole work in parallel.
     *
     * @param threads   number of {@code Threads} in which work should be done.
     * @param list      {@code List} which should be filtered
     * @param predicate a {@code non-interfering} and {@code stateless}
     *                  predicate to apply to each element to determine if it should be included
     * @param <T>       {@code parent} {@code generic} type of elements
     * @return minimum element in list in the sense of {@code comparator}
     * @throws InterruptedException if any worker thread was interrupted during its work
     */


    @Override
    public <T> List<T> filter(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<T, List<T>> action = x -> {
            ArrayList listR = new ArrayList<T>();
            if (predicate.test(x))
                listR.add(x);
            return listR;
        };
        Function<List<List<T>>, List<T>> combiner = listT -> {
            ArrayList<T> result = new ArrayList<>();
            for (List<T> x : listT)
                for (T y : x)
                    result.add(y);
            return result;
        };
        return parallel(threads, list, action, combiner);
    }


    /**
     * For the given {@code list} returns another {@code list} which consists
     * of the results of applying given {@code function} to the elements of the initial one.
     * <p>
     * For intrinsic purposes it uses {@code threads} of {@link java.lang.Thread}
     * objects. Each of them gets its own part of work to
     * perform the whole work in parallel.
     *
     * @param threads  number of {@code Threads} in which work should be done.
     * @param list     initial {@code List} of elements to which {@code function} must
     *                 be applied
     * @param function a {@code non-interfering} and {@code stateless}
     *                 function to apply to each element
     * @param <T>      {@code parent} {@code generic} type of elements
     * @return {@code List} of results of applying {@code function}
     * @throws InterruptedException if any worker thread was interrupted during its work
     */

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        Function<T, List<U>> action = x -> {
            ArrayList<U> result = new ArrayList<U>();
            result.add(function.apply(x));
            return result;
        };
        Function<List<List<U>>, List<U>> combiner = listT -> {
            ArrayList<U> result = new ArrayList<>();
            for (List<U> x : listT)
                for (U y : x)
                    result.add(y);
            return result;
        };
        return parallel(threads, list, action, combiner);
    }

    ;


    /**
     * For the given {@code list} returns maximum element in this list according to
     * {@code comparator}.
     * <p>
     * For intrinsic purposes it uses {@code threads} of
     * {@link java.lang.Thread} objects. Each of them gets its own part of work to
     * perform the whole work in parallel.
     *
     * @param threads    number of {@code Threads} in which work should be done.
     * @param list       {@code List} where to find minimum element
     * @param comparator comparator according to which compares performs
     * @param <T>        {@code parent} {@code generic} type of elements
     * @return maximum element in list in the sense of {@code comparator}
     * @throws InterruptedException if any worker thread was interrupted during its work
     */


    @Override
    public <T> T maximum(int threads, List<? extends T> list, final Comparator<? super T> comparator) throws InterruptedException {
        Function<T, T> action = a -> a;
        Function<List<T>, T> combiner = listT -> {
            T result = null;
            for (T aListT : listT) {
                if (aListT == null)continue;
                if (result == null || comparator.compare(result, aListT) == -1)
                    result = aListT;
            }
            return result;
        };
        return parallel(threads, list, action, combiner);
    }

    /**
     * For the given {@code list} returns minimum element in this list according to
     * {@code comparator}.
     * <p>
     * For intrinsic purposes it uses {@code threads} of
     * {@link java.lang.Thread} objects. Each of them gets its own part of work to
     * perform the whole work in parallel.
     *
     * @param threads    number of {@code Threads} in which work should be done.
     * @param list       {@code List} where to find minimum element
     * @param comparator comparator according to which compares performs
     * @param <T>        {@code parent} {@code generic} type of elements
     * @return minimum element in list in the sense of {@code comparator}
     * @throws InterruptedException if any worker thread was interrupted during his work
     */

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

    /**
     * Returns whether all elements of the given {@code list} match
     * the provided {@code predicate}.
     * <p>
     * For intrinsic purposes it uses {@code threads} of {@link java.lang.Thread}
     * objects. Each of them gets its own part of work to perform the whole work in parallel.
     *
     * @param threads   number of {@code Threads} in which work should be done.
     * @param list      {@code List} of elements to be checked
     * @param predicate a {@code non-interfering} and {@code stateless}
     *                  predicate to be check to each element
     * @param <T>       {@code parent} {@code generic} type of elements
     * @return {@code true} if all elements match the given {@code predicate},
     * {@code false} otherwise.
     * @throws InterruptedException if any worker thread was interrupted during its work
     */

    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<T, Boolean> action = predicate::test;
        Function<List<Boolean>, Boolean> combiner = listB -> listB.stream().filter(x -> !x).count() == 0;
        return parallel(threads, list, action, combiner);
    }

    /**
     * Returns whether any element of the given {@code list} matches
     * the provided {@code predicate}.
     * <p>
     * For intrinsic purposes it uses {@code threads} of {@link java.lang.Thread}
     * objects. Each of them gets its own part of work to perform the whole work in parallel.
     *
     * @param threads   number of {@code Threads} in which work should be done.
     * @param list      {@code List} of elements for which {@code predicate} should be checked
     * @param predicate a {@code non-interfering} and {@code stateless}
     *                  predicate to be checked
     * @param <T>       {@code parent} {@code generic} type of elements
     * @return {@code true} if any element of the {@code list} matches the given {@code predicate},
     * {@code false} otherwise.
     * @throws InterruptedException if any worker thread was interrupted during its work
     */

    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<T, Boolean> action = predicate::test;
        Function<List<Boolean>, Boolean> combiner = listB -> listB.stream().filter(x -> x).count() > 0;
        return parallel(threads, list, action, combiner);
    }
}
