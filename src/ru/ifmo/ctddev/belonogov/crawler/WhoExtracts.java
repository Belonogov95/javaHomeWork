package ru.ifmo.ctddev.belonogov.crawler;

import java.io.IOException;
import java.util.List;

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
            List<String> list = extractTask.getDocument().extractLinks();
            for (String s : list) {
                webCrawler.addDownloadTask(s, extractTask.getDepth(), extractTask.getMyResult());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        extractTask.getMyResult().decCountInQueue();
//        System.err.println("inQueue extr: " + extractTask.getMyResult().getCountInQueue());
        synchronized (extractTask.getMyResult()) {
            if (extractTask.getMyResult().getCountInQueue() == 0)
                extractTask.getMyResult().notify();
        }
    }
}
