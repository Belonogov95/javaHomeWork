package ru.ifmo.ctddev.belonogov.arrayset;

import com.sun.javafx.image.IntPixelGetter;

import java.util.*;

public class Main {

    static void dump(Collection<Object> c) {
        for (Iterator<Object> i = c.iterator(); i.hasNext(); ) {
            Object o = i.next();
            System.out.println(o);
        }
    }

    static void f(List<? extends Number> a) {
        for (Number x : a)
            System.out.println(x.intValue() + 1);
    }
    //static int[] data = {-1958721717, 549063946, -754945295, -1774440888, 1029409260, -1577587722, 1449556680, 2021934269, 761216240, 1137816627};
    int [] data2 = {1047730974, -374666944, 1986083359, -1596718559};

    static class MyComparator < T > implements Comparator < T > {
        @Override
        public int compare(T o1, T o2) {
            return 0;
        }
    }

    public void run() {
        LinkedHashSet < Integer > x = new LinkedHashSet<>();
        Random random = new Random(19);
        for (int i = 0; i < 10; i++)
            x.add(random.nextInt(100));
        ArraySet < Integer > a = new ArraySet<>(x);
        a.print();
        int key = 45;
        System.out.println(a.lower(key));
        System.out.println(a.floor(key));
        System.out.println(a.ceiling(key));
        System.out.println(a.higher(key));


//        List < Integer > a = new ArrayList<>();
//        for (int i = 0; i < data2.length; i++)
//            a.add(data2[i]);
//        MyComparator < Integer > comparator = new MyComparator<>();
//        int res = Collections.binarySearch(a, new Integer(588)); //, comparator);
//        System.out.println(res);

//        LinkedHashSet < Integer > b = new LinkedHashSet<>();
//        Random random = new Random(19);
//        for (int i = 0; i < data2.length; i++)
//            b.add(data2[i]);
//        //for (int i = 0; i < 10; i++)
//            //b.add(random.nextInt(100));
//        //Collection < Integer > c = b;
//
//
//        ArraySet < Integer > a = new ArraySet< Integer >(b); //, new MyComparator<Integer>());
//        //int key = 44;
//        a.print();
//        NavigableSet<Integer> e = a.tailSet(1986083359, false);
//
////
////        ArrayList < Box > a = new ArrayList<>();
////        for (int i = 0; i < 20; i++)
//            a.add(new Box((int)(Math.random() * 100)));
//        Comparator < Box > cmp = new Comparator<Box>() {
//            @Override
//            public int compare(Box o1, Box o2) {
//                if (o1.getT().equals(o2.getT())) return 0;
//                return (o1.getT() < o2.getT())? -1: 1;
//            }
//        };
//        TreeSet < Box > d = new TreeSet<>();
//        d.add(new Box(10));
        //Collections.sort(a, cmp);
        //for (Box x: a)
            //System.out.println(x.getT());

    }

    public static void main(String[] args) {
        new Main().run();

    }
}
