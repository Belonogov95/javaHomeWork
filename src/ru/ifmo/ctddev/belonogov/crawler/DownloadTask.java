package ru.ifmo.ctddev.belonogov.crawler;

/**
 * Created by vanya on 15.04.15.
 */

class DownloadTask {
    String url;
    int depth;
    WorkResult result;

    public DownloadTask(String url, int depth, WorkResult result) {
        this.url = url;
        this.depth = depth;
        this.result = result;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public WorkResult getResult() {
        return result;
    }

    public void setResult(WorkResult result) {
        this.result = result;
    }
};

