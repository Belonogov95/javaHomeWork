package ru.ifmo.ctddev.belonogov.helloUDP;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vanya on 17.05.15.
 */
public class HelloUDPClient implements HelloClient {

    @Override
    public void start(String host, int port, String prefix, int requests, int threads) {
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        Counter counter = new Counter(threads);
        for (int i = 0; i < threads; i++)
            threadPool.execute(new Writer(host, port, prefix, requests, i, counter));

        synchronized (counter) {
            while (!counter.isZero())
                try {
                    counter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        threadPool.shutdown();
    }

    public static void main(String[] args) {
        assert(args.length == 5);
        for (int i = 0; i < 5; i++)
            assert(args[i] != null);
        (new HelloUDPClient()).start(args[0], Integer.valueOf(args[1]), args[2], Integer.valueOf(args[3]), Integer.valueOf(args[4]));
    }

}
