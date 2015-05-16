package ru.ifmo.ctddev.belonogov.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;

import java.io.IOException;

public class WhoDownloads implements Runnable {
    private String host;
    private WebCrawler webCrawler;

    public WhoDownloads(String host, WebCrawler webCrawler) {
        this.host = host;
        this.webCrawler = webCrawler;
    }

    @Override
    public void run() {
        assert(webCrawler.getHostStatus().containsKey(host));
        HostQueue hostQueue = webCrawler.getHostStatus().get(host);
        assert(!hostQueue.getQueue().isEmpty());
        DownloadTask task = hostQueue.getQueue().poll();
        hostQueue.setIsInExecutorQueue(false);
        webCrawler.updateHost(host);

        assert(task != null);
        Document document;
        try {
            document = webCrawler.getDownloader().download(task.getUrl());
            task.getMyResult().getDownloadedPages().add(task.getUrl());
            if (task.getDepth() - 1 > 0) {
                task.getMyResult().incCountInQueue();
                webCrawler.getExtractorsPool().execute(new WhoExtracts(webCrawler, new ExtractTask(document, task.getDepth() - 1, task.getMyResult())));
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
