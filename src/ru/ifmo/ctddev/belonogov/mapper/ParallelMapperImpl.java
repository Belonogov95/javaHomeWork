package ru.ifmo.ctddev.belonogov.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

///


public class ParallelMapperImpl implements ParallelMapper {
    ArrayList<Thread> threads;
    final Queue<Task> queue;


    private class Task<T, R> {
        private Function<T, R> f;
        private T value;
        private CountTask countTask;
        private R result;

        public Task(Function<T, R> f, T value, CountTask countTask) {
            this.f = f;
            this.value = value;
            this.countTask = countTask;
        }

        public void setResult(R result) {
            this.result = result;
        }

        public R getResult() {
            return result;
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
        private int threadId;
        Object result;

        public Worker(int threadId) {
            this.threadId = threadId;
        }

        public Object getResult() {
            return result;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (queue) {
                    //System.err.println(queue);
                    try {
                        //System.err.println("before while");
                        while (queue.isEmpty())
                            queue.wait();
                        //System.err.println("after while");
                        Task myTask = queue.remove();
                        myTask.setResult(myTask.f.apply(myTask.value));
                        //System.err.println("new value: " + myTask.result);
                        synchronized (myTask.countTask) {
                            myTask.countTask.dec();
                            if (myTask.countTask.isZero())
                                myTask.countTask.notify();
                        }
                    } catch (InterruptedException e) {
                        System.err.println("close ");
                        return;
                    }
                }
            }
        }
    }


    public ParallelMapperImpl(int countThreads, Queue < Task > queue) {
        this.queue = queue;
        threads = new ArrayList<>();
        for (int i = 0; i < countThreads; i++) {
            threads.add(new Thread(new Worker(i)));
        }
        //queue = new LinkedList<Task>();
        for (int i = 0; i < countThreads; i++)
            threads.get(i).start();
    }



    //public static void main(String[] args) {
        //(new ParallelMapperImpl()).run();
    //}


    @Override
    public <T, R> List<R> run(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        System.err.println("start run");
        CountTask countTask = new CountTask(args.size());
        List<Task> list = new ArrayList<Task>();
        synchronized (queue) {
            for (T arg : args) {
                Task tmp = new Task(f, arg, countTask);
                list.add(tmp);
                queue.add(tmp);
            }
            queue.notifyAll();
        }
        synchronized (countTask) {
            //System.err.println("count " + countTask.count);
            while (!countTask.isZero()) {
                countTask.wait();
            }
            //System.err.println("count " + countTask.count);
            List<R> result = new ArrayList<R>();
            for (Task task : list) {
                //System.err.println("\told new: " + task.value + " " + task.result);
                //assert(task.getResult() != null);
                result.add((R) task.getResult());
            }
            return result;
        }
    }

    @Override
    public void close() throws InterruptedException {
        for (Thread thread: threads)
            thread.interrupt();
    }
}
