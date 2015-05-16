package ru.ifmo.ctddev.belonogov.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import org.junit.internal.runners.statements.RunBefores;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vanya on 15.05.15.
 */
public class WhoExtracts implements Runnable {
    private WebCrawler webCrawler;
    private ExtractTask extractTask;

    public WhoExtracts(WebCrawler webCrawler, ExtractTask extractTask) {
        this.webCrawler = webCrawler;
        this.extractTask = extractTask;
    }

    @Override
    public void run() {
        try {
            List< String > list = extractTask.getDocument().extractLinks();
            for (String s: list) {
                //extractTask.getMyResult().incCountInQueue();
                webCrawler.addDownloadTask(s, extractTask.getDepth(), extractTask.getMyResult());
            }
        } catch (IOException e) {
            assert(false);
        }
        extractTask.getMyResult().decCountInQueue();
//        System.err.println("inQueue extr: " + extractTask.getMyResult().getCountInQueue());
        synchronized (extractTask.getMyResult()) {
            if (extractTask.getMyResult().getCountInQueue() == 0)
                extractTask.getMyResult().notify();
        }
    }
}
