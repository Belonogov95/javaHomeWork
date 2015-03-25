package ru.ifmo.ctddev.belonogov;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vanya on 25.03.15.
 */
public class Main1 {
    void run() {
        System.err.println("sdfasdfa");
        //assert(false);
        ParallelMapperImpl mapper = new ParallelMapperImpl(2, new LinkedList<>());
        //if (true) return;
        int[] data = {2, 4, 3, 6, 4, 3};
        List< Integer > list = new ArrayList<Integer>();
        for (int x: data)
            list.add(x);
        try {
            List < Integer > result = mapper.run(x -> x + 2, list);
            List < String > stringR = mapper.run(x -> x.toString() + "!key!", list);
            for (Integer x: result)
                System.out.println(x);
            for (String s: stringR)
                System.out.println(s);
            mapper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String [] args) {
        new Main1().run();
    }


}
