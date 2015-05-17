package ru.ifmo.ctddev.belonogov.helloUDP;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by vanya on 17.05.15.
 */
public class HelloUDPServer implements HelloServer {
    private static final int T = 1000;
    private static final int MAX_QUEUE_SIZE = 1000;
    ConcurrentHashMap < Integer, ExecutorService > allExecutorServices;
    ConcurrentHashMap < DatagramSocket, Boolean > allSockets;


    public HelloUDPServer() {
        allExecutorServices = new ConcurrentHashMap<>();
        allSockets = new ConcurrentHashMap<>();
    }

    @Override
    public void start(int port, int threads) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        assert socket != null;

        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        allExecutorServices.put(port, threadPool);
        allSockets.put(socket, true);
        for (int i = 0; i < threads; i++)
            threadPool.execute(new Listener(socket));
    }

    @Override
    public void close() {
        for (Map.Entry < Integer, ExecutorService > entry: allExecutorServices.entrySet()) {
            entry.getValue().shutdown();
        }
    }

    public static void main(String [] args) {
        (new HelloUDPServer()).start(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
    }

}
