package ru.ifmo.ctddev.belonogov.helloUDP;

/**
 * Created by vanya on 17.05.15.
 */
public class Counter {
    public int count;

    public Counter(int count) {
        this.count = count;
    }
    public void dec()  {
        count--;
    }
    public boolean isZero() {
        return count == 0;
    }
};
