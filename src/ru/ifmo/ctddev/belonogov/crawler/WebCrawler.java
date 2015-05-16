package ru.ifmo.ctddev.belonogov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements Crawler {
    private ExecutorService downloadersPool;
    private ExecutorService extractorsPool;
    private Downloader downloader;
    private ConcurrentHashMap < String, HostQueue > hostStatus;
    private ConcurrentHashMap < String, Boolean > viewedPages;
    private ConcurrentHashMap < MyResult, Boolean > allMyResults;
    private BlockingQueue < Runnable > downloadQueue;
    private int perHost;

    public Downloader getDownloader() {
        return downloader;
    }

    public ConcurrentHashMap<String, HostQueue> getHostStatus() {
        return hostStatus;
    }

    public ExecutorService getExtractorsPool() {
        return extractorsPool;
    }

    public ExecutorService getDownloadersPool() {
        return downloadersPool;
    }

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
        System.err.println(url);
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

// ExecutorService downloadersPool;
//    ExecutorService extractorsPool;
//    Downloader downloader;
//    ConcurrentHashMap < String, HostQueue > hostStatus;
//    ConcurrentHashMap < String, Boolean > viewedPages;
//    ConcurrentHashMap < MyResult, Boolean > allMyResults;
//    BlockingQueue < Runnable > downloadQueue;
//    int perHost;


    public static void main(String [] args) {
        String url = args[0];
        int downloaders = Integer.MAX_VALUE;
        int extractors = Integer.MAX_VALUE;
        int perHost= Integer.MAX_VALUE;
        int depth = 1;
        if (args.length >= 2 && args[1] != null) {
            downloaders = Integer.valueOf(args[1]);
        }
        if (args.length >= 3 && args[2] != null) {
            extractors = Integer.valueOf(args[2]);
        }
        if (args.length >= 4 && args[3] != null) {
            perHost = Integer.valueOf(args[3]);
        }
        try {
            System.err.println("url : " + url);
            WebCrawler wc = new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost);
            wc.download(url, 1);
            wc.close();
        } catch (IOException e) {
            e.printStackTrace();
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
