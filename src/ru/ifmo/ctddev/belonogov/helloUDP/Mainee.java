package ru.ifmo.ctddev.belonogov.helloUDP;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by vanya on 16.05.15.
 */
public class Mainee {

    void f() {
//        try {
////            InetAddress adr = InetAddress.getByName("8.8.8.8");
//            String adr = InetAddress.getByName("ya.ru").getHostName();
//            System.err.println(adr);
//            adr = InetAddress.getByName("google.ru").getCanonicalHostName();
//            System.err.println(adr);
//
//            //System.err.println("after");
//            //System.err.println(adr.length);
////            for (InetAddress x: adr)
////                System.err.println(x);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        String x = "abacaba";
//        for (byte y: x.getBytes())
//            System.err.println(y + " ");
        HelloClient client = new HelloUDPClient();
        client.start("localhost", 56712, "-----abacaba-------", 5, 5);
    }

    public static void main(String [] args) {
        new Mainee().f();
    }

}
