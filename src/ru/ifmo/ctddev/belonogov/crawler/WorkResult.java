package ru.ifmo.ctddev.belonogov.crawler;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vanya on 15.04.15.
 */
public class WorkResult {
//    private int balance;
    private AtomicInteger balance;

    private ArrayList<String> pages;


    public WorkResult() {
        pages = new ArrayList<>();
        balance = new AtomicInteger(0);
    }

    public void inc() {
        balance.incrementAndGet();
    }

    public void dec() {
        balance.decrementAndGet();
        assert(balance.get() >= 0);
    }

    public boolean isZero() {
        return balance.get() == 0;
    }

    public ArrayList<String> getResult() {
        return pages;
    }
    public int getBalance() {
        return balance.intValue();
    }

}

