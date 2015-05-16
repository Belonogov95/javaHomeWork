package ru.ifmo.ctddev.belonogov.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;

/**
 * Created by vanya on 15.05.15.
 */
public class ExtractTask {
    private Document document;
    private int depth;
    private MyResult myResult;

    public Document getDocument() {
        return document;
    }

    public int getDepth() {
        return depth;
    }

    public MyResult getMyResult() {
        return myResult;
    }

    public ExtractTask(Document document, int depth, MyResult myResult) {

        this.document = document;
        this.depth = depth;
        this.myResult = myResult;
    }
}
