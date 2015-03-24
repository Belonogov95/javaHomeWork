package ru.ifmo.ctddev.belonogov.arrayset;

import java.util.TreeSet;

/**
 * Created by vanya on 24.02.15.
 */
public class Box {
    private Integer t;
    public Integer getT() { return t;}
    public Box(Integer t) {
        this.t = t;
    }
    void setT(Integer t) {
        this.t = t;
    }
    TreeSet< Integer > k = new TreeSet<>();

    void print() {
        System.out.println(t);
    }
    void printType() {
        System.out.println(t.getClass().getName());
    }


    public int compareTo(Box o) {
        return (t < o.getT())? 1: 0;
    }
}
