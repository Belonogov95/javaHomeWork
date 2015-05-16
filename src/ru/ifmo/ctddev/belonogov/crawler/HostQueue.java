package ru.ifmo.ctddev.belonogov.crawler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vanya on 15.05.15.
 */
public class HostQueue {
    private AtomicInteger countFreeSpace;
    private ConcurrentLinkedQueue < DownloadTask > queue;
    private boolean isInExecutorQueue;


    public HostQueue(AtomicInteger countFreeSpace, ConcurrentLinkedQueue <DownloadTask > queue) {
        this.countFreeSpace = countFreeSpace;
        this.queue = queue;
        this.isInExecutorQueue = false;
    }

    public void setIsInExecutorQueue(boolean isInExecutorQueue) {
        this.isInExecutorQueue = isInExecutorQueue;
    }


    public void incCountFreeSpace() {
        countFreeSpace.incrementAndGet();
    }

    public void decCountFreeSpace() {
        assert(countFreeSpace.get() > 0);
        countFreeSpace.decrementAndGet();
    }

    public boolean isInExecutorQueue() {
        return isInExecutorQueue;
    }

    public int getCountFreeSpace() {
        return countFreeSpace.get();
    }

    public ConcurrentLinkedQueue<DownloadTask> getQueue() {
        return queue;
    }


}
