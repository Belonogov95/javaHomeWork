package ru.ifmo.ctddev.belonogov.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import javax.jws.WebResult;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by vanya on 08.04.15.
 */
public class WebCrawler implements Crawler {
    //private final HashMap<String, Queue<DownloadTask>> downloadQueue;
//    private final HashMap<String, Integer> numberOfDownloads;

    private final ConcurrentHashMap <String, Queue<DownloadTask>> downloadQueue;
    private final Queue<ExtractTask> extractQueue;
    private final ConcurrentHashMap<String, Integer> numberOfDownloads;
    private int perHost;
    private Downloader downloader;
    private boolean closed;
    private ArrayList < Thread > allThread;


    private class MyLoader implements Runnable {

        @Override
        public void run() {
            while (true) {
                DownloadTask downloadTask;
                String key;
                if (closed) return;
                synchronized (downloadQueue) {
                    while (true) {
                        try {
                            downloadQueue.wait();
                        } catch (InterruptedException e) {
                            return;
                            //assert (false);
                        }
                        /// possible dead lock
                        key = null;
                        synchronized (numberOfDownloads) {
                            for (Map.Entry<String, Queue<DownloadTask>> x : downloadQueue.entrySet()) {
                                if (!numberOfDownloads.containsKey(x.getKey()) || numberOfDownloads.get(x.getKey()) < perHost) {
                                    key = x.getKey();
                                    break;
                                }
                            }
                        }
                        if (key != null) {
                            downloadTask = downloadQueue.get(key).poll();
                            if (downloadQueue.get(key).size() == 0) {
                                downloadQueue.remove(key);
                            }
                            break;
                        }
                    }
                }
//                System.err.println("-download Task: " + downloadTask.getUrl());
                Document doc = null;
                synchronized (numberOfDownloads) {
                    if (!numberOfDownloads.containsKey(key)) {
                        numberOfDownloads.put(key, 0);
                    }
                    numberOfDownloads.put(key, numberOfDownloads.get(key) + 1);
                }

                try {
                    doc = downloader.download(downloadTask.getUrl());
                } catch (IOException e) {
                    e.printStackTrace();
                    //assert (false);
                }
//                System.err.println("-finish");
                synchronized (numberOfDownloads) {
                    //assert(numberOfDownloads.get(key) > 0);
                    numberOfDownloads.put(key, numberOfDownloads.get(key) - 1);
                    if (numberOfDownloads.get(key) == 0) {
                        numberOfDownloads.remove(key);
                    }
                }

//                System.err.println("depth " + downloadTask.getDepth());
                if (downloadTask.getDepth() > 1) {
                    downloadTask.getResult().inc();
                    synchronized (extractQueue) {
                        extractQueue.add(new ExtractTask(doc, downloadTask.getDepth() - 1, downloadTask.getResult()));
                        System.err.println("1");
                        extractQueue.notify();
                        System.err.println("2");
                    }
                }
//                System.err.println("add and notify");
                downloadTask.getResult().dec();


                WorkResult result = downloadTask.getResult();
                synchronized (result) {
                    if (result.isZero()) {
                        result.notify();
                    }
                }



//                System.err.println("downloadTaks get result " + downloadTask.getResult().getBalance());
                synchronized (downloadTask) {
                    downloadTask.getResult().getResult().add(downloadTask.getUrl());
                }
            }
        }
    }

    private class MyExtract implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (closed) return;

                synchronized (extractQueue) {
                    while (extractQueue.isEmpty()) {
//                        System.err.println("extra queue");
                        try {
                            extractQueue.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
//                        Syste.err.println("stop wait");
                    }
                    ExtractTask extractTask = extractQueue.poll();
                    System.err.println("extr task " + extractTask.getDocument().toString());
                    List < String > links = null;
                    try {
                        links = extractTask.getDocument().extractLinks();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    synchronized (downloadQueue) {
                        for (String s: links) {
                            addPage(new DownloadTask(s, extractTask.getDepth(), extractTask.getWorkResult()));
                        }
                    }
                    extractTask.getWorkResult().dec();

                    WorkResult result = extractTask.getWorkResult();
                    synchronized (result) {
                        if (result.isZero()) {
                            result.notify();
                        }
                    }

                }
            }
        }
    }

    void addPage(DownloadTask downloadTask) {
        synchronized (downloadQueue) {
            String host = null;
            try {
                host = URLUtils.getHost(downloadTask.getUrl());
                if (!downloadQueue.containsKey(host)) {
                    downloadQueue.put(host, new LinkedList<>());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                //assert(false);
            }
            downloadTask.getResult().inc();
            downloadQueue.get(host).add(downloadTask);
            downloadQueue.notify();
        }
    }


    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        downloaders = Math.min(downloaders, 100);
        extractors = Math.min(extractors, 100);
        System.err.println("create: -----------------------------");
        System.err.println("downloaders " + downloaders + " " + extractors + " " + perHost);
        this.downloader = downloader;
        this.perHost = perHost;
        this.closed = false;
        downloadQueue = new ConcurrentHashMap<>();
        numberOfDownloads = new ConcurrentHashMap<>();
        extractQueue = new LinkedList<>();
        allThread = new ArrayList<>();

        for (int i = 0; i < downloaders; i++) {
            Thread tmp = new Thread(new MyLoader());
            allThread.add(tmp);
            tmp.start();
        }
        for (int i = 0; i < extractors; i++) {
            Thread tmp = new Thread(new MyExtract());
            allThread.add(tmp);
            tmp.start();
        }
    }

    @Override
    public List<String> download(String url, int depth) throws IOException {
        System.err.println("query: " + url + " " + depth);
        //assert (false);
        //System.err.println("query: " + url + " " + depth);
        if (closed) {
            return null;
        }
        WorkResult workResult = new WorkResult();
        addPage(new DownloadTask(url, depth, workResult));

        synchronized (workResult) {
            while (!workResult.isZero()) {
                try {
                    workResult.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //assert(false);
                }
            }
            return workResult.getResult();
        }
    }

    @Override
    public void close() {
        closed = true;
        synchronized (downloadQueue) {
            downloadQueue.clear();
        }
        synchronized (extractQueue) {
            extractQueue.clear();
        }
        for (Thread x: allThread)
            x.interrupt();
    }

}
