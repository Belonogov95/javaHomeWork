package ru.ifmo.ctddev.belonogov.crawler;

/**
 * Created by vanya on 15.05.15.
 */
public class DownloadTask {
    private String url;
    private int depth;
    private final MyResult myResult;

    public DownloadTask(String url, int depth, MyResult myResult) {
        this.url = url;
        this.depth = depth;
        this.myResult = myResult;
    }

    public String getUrl() {
        return url;
    }

    public int getDepth() {
        return depth;
    }

    public MyResult getMyResult() {
        return myResult;
    }
}
