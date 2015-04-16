package ru.ifmo.ctddev.belonogov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import ru.ifmo.ctddev.belonogov.concurrent.IterativeParallelism;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by vanya on 25.03.15.
 */
public class Main1 {

//    public < T > String f(String g) {
//        T x = (T)g;
//        Integer y = (Integer)x;
//        System.out.println("y: " + y);
//        return y.toString();
//    }

    public void run() {
        ParallelMapperImpl mapper = new ParallelMapperImpl(3);
        IterativeParallelism ip = new IterativeParallelism(mapper);
        //for (int i = 0; i < 1000;)
        int[] data = {2, 4, 3, 6, 4, 3};
        List< Integer > list = new ArrayList<Integer>();
        for (int x: data)
            list.add(x);
        try {
            ip.concat(3, list);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            List < Integer > result = mapper.map(x -> x + 2, list);
            List < String > stringR = mapper.map(x -> x.toString() + "!key!", list);
            for (Integer x: result)
                System.out.println(x);
            for (String s: stringR)
                System.out.println(s);
            mapper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void run2() {
        ParallelMapper mapper = new ParallelMapperImpl(9);
        Function< Integer, Integer > alf = (x) -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return x + 1;
        };
        ArrayList < Integer > a = new ArrayList<>();
        int n = 100;
        long ts = System.nanoTime();
        for (int i = 0; i < n; i++)
            a.add(new Integer(1));
        System.err.println("start");
        try {
            mapper.map(alf, a);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.err.println("finish");
        long tf = System.nanoTime();
        System.err.println("time: " + (tf - ts) / 1e9);
    }

    public static void main(String [] args) {
        //new Main1().run();
        new Main1().run2();
    }


}
