package ru.ifmo.ctddev.belonogov.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by vanya on 15.05.15.
 */
public class WhoDownloads implements Runnable {
    private String host;
    private WebCrawler webCrawler;

    public WhoDownloads(String host, WebCrawler webCrawler) {
        this.host = host;
        this.webCrawler = webCrawler;
    }

    @Override
    public void run() {
        assert(webCrawler.hostStatus.containsKey(host));
        HostQueue hostQueue = webCrawler.hostStatus.get(host);
        assert(!hostQueue.getQueue().isEmpty());
        DownloadTask task = hostQueue.getQueue().poll();
        hostQueue.setIsInExecutorQueue(false);
        webCrawler.updateHost(host);

        //System.err.println("task: " + task);
        assert(task != null);
        Document document;
        try {
            document = webCrawler.downloader.download(task.getUrl());
            task.getMyResult().getDownloadedPages().add(task.getUrl());
            if (task.getDepth() - 1 > 0) {
                task.getMyResult().incCountInQueue();
                webCrawler.extractorsPool.execute(new WhoExtracts(webCrawler, new ExtractTask(document, task.getDepth() - 1, task.getMyResult())));
            }
        } catch (IOException e) {
            task.getMyResult().getErrors().put(task.getUrl(), e);
        }
        hostQueue.incCountFreeSpace();

        webCrawler.updateHost(host);

        assert(task.getMyResult() != null);
        task.getMyResult().decCountInQueue();

//        System.err.println("inQueue download: " + task.getMyResult().getCountInQueue() + " suminQueue " + webCrawler.getSumInHostQueue()
//         + " execute: "  + webCrawler.getSumInExecuteQueue() + " real execute Size: " + webCrawler.downloadQueue.size());

        synchronized (task.getMyResult()) {
            if (task.getMyResult().getCountInQueue() == 0)
                task.getMyResult().notify();
        }
    }
}
