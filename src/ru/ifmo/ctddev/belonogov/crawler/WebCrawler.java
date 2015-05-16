package ru.ifmo.ctddev.belonogov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements Crawler {
    ExecutorService downloadersPool;
    ExecutorService extractorsPool;
    Downloader downloader;
    ConcurrentHashMap < String, HostQueue > hostStatus;
    ConcurrentHashMap < String, Boolean > viewedPages;
    ConcurrentHashMap < MyResult, Boolean > allMyResults;
    BlockingQueue < Runnable > downloadQueue;
    int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.perHost = perHost;
        this.downloader = downloader;
        downloadQueue = new LinkedBlockingQueue<>();
        downloadersPool = new ThreadPoolExecutor(downloaders, downloaders, 1, TimeUnit.SECONDS, downloadQueue);
        extractorsPool = Executors.newFixedThreadPool(extractors);
        hostStatus = new ConcurrentHashMap<>();
        viewedPages = new ConcurrentHashMap<>();
        allMyResults = new ConcurrentHashMap<>();
    }



    void addDownloadTask(String url, int depth, MyResult myResult) {
        if (viewedPages.containsKey(url)) return;
        viewedPages.put(url, true);

        String host = null;
        try {
            host = URLUtils.getHost(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        assert(host != null);
        if (!hostStatus.containsKey(host)) {
            hostStatus.put(host, new HostQueue(new AtomicInteger(perHost), new ConcurrentLinkedQueue<>()));
        }
        HostQueue hostQueue = hostStatus.get(host);
        myResult.incCountInQueue();
        hostQueue.getQueue().add(new DownloadTask(url, depth, myResult));

        updateHost(host);
    }

    @Override
    public Result download(String url, int depth) {
        MyResult myResult = new MyResult();
        allMyResults.put(myResult, true);
        addDownloadTask(url, depth, myResult);

        synchronized (myResult) {
            while (myResult.getCountInQueue() > 0) {
                try {
                    myResult.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Map < String, IOException > map = new TreeMap<>();
            for (Map.Entry < String, IOException > tmp: myResult.getErrors().entrySet())
                map.put(tmp.getKey(), tmp.getValue());
            allMyResults.remove(myResult);
            return new Result(myResult.getDownloadedPages(), map);
        }
    }

    @Override
    public void close() {
        downloadersPool.shutdown();
        extractorsPool.shutdown();
//        System.err.println("-----------download isn't complete " + allMyResults.size());
        for (Map.Entry < MyResult, Boolean > tmp: allMyResults.entrySet()) {
            MyResult myResult = tmp.getKey();
            synchronized (myResult) {
                myResult.setCountInQueue(0);
                myResult.notify();
            }
        }

    }

    public void updateHost(String host) {
        assert(hostStatus.containsKey(host));
        HostQueue hostQueue = hostStatus.get(host);
        synchronized (hostQueue) {
            if (!hostQueue.isInExecutorQueue() && hostQueue.getCountFreeSpace() > 0 && !hostQueue.getQueue().isEmpty()) {
                hostQueue.setIsInExecutorQueue(true);
                hostQueue.decCountFreeSpace();
                downloadersPool.execute(new WhoDownloads(host, this));
            }
        }
    }
//
//    public int getSumInHostQueue() {
//        int sum = 0;
//        for (Map.Entry <String, HostQueue > tmp: hostStatus.entrySet())
//            sum += tmp.getValue().getQueue().size();
//        return sum;
//    }
//
//    public int getSumInExecuteQueue() {
//        int sum = 0;
//        for (Map.Entry <String, HostQueue > tmp: hostStatus.entrySet())
//            if (!tmp.getValue().isInExecutorQueue())
//                sum++;
//        return sum;
//    }

}
