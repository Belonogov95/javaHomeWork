package ru.ifmo.ctddev.belonogov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;



/**
 * Implementation of {@link info.kgeorgiy.java.advanced.mapper.ParallelMapper}.
 * <p>
 * This class is kind of a simple thread pool. Its main method
 * {@link #map(java.util.function.Function, java.util.List)} divides
 * potentially heavy work for number of {@code Threads} specified at constructor
 * and does the work in parallel.
 */

public class ParallelMapperImpl implements ParallelMapper {
    ArrayList<Thread> threads;
    final Queue<Task<?, ?>> queue;
    Boolean closeOccur = false;

    private class Task<T, R> implements Runnable {
        private Function<T, R> f;
        private T value;
        private final CountTask countTask;
        private Boolean flagKill;
        private R result;

        public Task(Function<T, R> f, T value, CountTask countTask, Boolean flagKill) {
            this.f = f;
            this.value = value;
            this.countTask = countTask;
            this.flagKill = flagKill;
        }

        public Boolean getFlagKill() {
            return flagKill;
        }

        public R getResult() {
            return result;
        }

        @Override
        public void run() {
            result = f.apply(value);
        }
    }

    private class CountTask {
        private int count;

        public CountTask(int count) {
            this.count = count;
        }

        public void dec() {
            count--;
        }

        public boolean isZero() {
            return count == 0;
        }
    }


    private class Worker implements Runnable {

        @Override
        public void run() {
            while (true) {
                Task<?, ?> myTask;
                synchronized (queue) {
                    //System.err.println(queue);
                    try {
                        //System.err.println("before while");
                        while (queue.isEmpty())
                            queue.wait();
                        //System.err.println("after while");
                        myTask = queue.remove();
                    } catch (InterruptedException e) {
                        //System.err.println("close ");
                        return;
                    }
                }
                if (myTask.getFlagKill()) {
                    System.err.println("close");
                    return;
                }
                myTask.run();
                //myTask.setResult(myTask.f.apply(myTask.value));
                synchronized (myTask.countTask) {
                    myTask.countTask.dec();
                    if (myTask.countTask.isZero())
                        myTask.countTask.notify();
                }

            }
        }
    }


    /**
     * Create an instance of thread pool with specified number of worker {@code threads}.
     *
     * @param countThreads number of threads to which work should be divided
     */


    public ParallelMapperImpl(int countThreads) {
        threads = new ArrayList<>();
        for (int i = 0; i < countThreads; i++) {
            threads.add(new Thread(new Worker()));
        }
        queue = new LinkedList<>();
        for (int i = 0; i < countThreads; i++)
            threads.get(i).start();
    }


    /**
     * Apply given {@code function} to given {@code list} of arguments and do that work
     * in parallel. Number of threads specified at constructor will be number of
     * potentially workers that pops queued tasks by readiness.
     *
     * @param f    action to be applied to the given {@code list} of arguments
     * @param args list to be mapped
     * @param <T>  type of all the arguments in the {@code list}
     * @param <R>  type of the result of the applying function
     * @return {@code List} of mapped elements
     * @throws InterruptedException if {@link #close()} was called before calculations were finished
     */

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        //System.err.println("start map");
        if (closeOccur)
            throw new IllegalStateException("mapper already closed");

        final CountTask countTask = new CountTask(args.size());
        List<Task> list = new ArrayList<>();
        synchronized (queue) {
            for (T arg : args) {
                Task<?, ?> tmp = new Task<>(f, arg, countTask, false);
                list.add(tmp);
                queue.add(tmp);
            }
            queue.notifyAll();
        }
        synchronized (countTask) {
            while (!countTask.isZero()) {
                countTask.wait();
            }
            List<R> result = new ArrayList<>();
            for (Task task : list) {
                result.add((R) task.getResult());
            }
            return result;
        }
    }

    /**
     * Forces all worker threads to terminate their activity (that is stop performing queued tasks)
     *
     * @throws InterruptedException if one of worker threads cannot be stopped
     */

    @Override
    public void close() throws InterruptedException {
        closeOccur = true;
        synchronized (queue) {
            for (Thread ignored : threads)
                queue.add(new Task<>(null, null, null, true));
            queue.notifyAll();
        }
    }
}
