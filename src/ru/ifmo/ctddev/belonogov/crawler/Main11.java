package ru.ifmo.ctddev.belonogov.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;

import java.io.IOException;

public class Main11 {
    /**
     *
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {
//        assert(false);
        WebCrawler webCrawler = new WebCrawler(new Downloader() {
            @Override
            public Document download(String url) throws IOException {
                return null;
            }
        }, 1, 1, 1);

        webCrawler.close();

        //webCrawler.download("https://www.google.ru", 3);
    }
}
