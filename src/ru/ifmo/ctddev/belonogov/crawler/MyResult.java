package ru.ifmo.ctddev.belonogov.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vanya on 15.05.15.
 */
public class MyResult {
    private List< String > downloadedPages;
    private ConcurrentHashMap < String, IOException > errors;
    AtomicInteger countInQueue;

    MyResult() {
        errors = new ConcurrentHashMap<>();
        downloadedPages = Collections.synchronizedList(new ArrayList<>());
        countInQueue = new AtomicInteger(0);
    }

    public List<String> getDownloadedPages() {
        return downloadedPages;
    }

    public ConcurrentHashMap<String, IOException> getErrors() {
        return errors;
    }

    public int getCountInQueue() {
        return countInQueue.get();
    }

    public void incCountInQueue() {
        countInQueue.incrementAndGet();
    }
    public void decCountInQueue() {
        countInQueue.decrementAndGet();
    }

    public void setCountInQueue(int countInQueue) {
        this.countInQueue = new AtomicInteger(countInQueue);
    }
}
