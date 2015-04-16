package ru.ifmo.ctddev.belonogov.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;

/**
 * Created by vanya on 16.04.15.
 */
public class ExtractTask {
    private Document document;
    private int depth;
    private WorkResult workResult;

    public ExtractTask(Document document, int depth, WorkResult workResult) {
        this.document = document;
        this.depth = depth;
        this.workResult = workResult;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public WorkResult getWorkResult() {
        return workResult;
    }

    public void setWorkResult(WorkResult workResult) {
        this.workResult = workResult;
    }
}
