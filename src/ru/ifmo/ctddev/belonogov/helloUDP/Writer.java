package ru.ifmo.ctddev.belonogov.helloUDP;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by vanya on 17.05.15.
 */
public class Writer implements Runnable {
    private String host;
    private int port;
    private String prefix;
    private int requests;
    private int threadId;
    private int myPort;
    private final static int MAGIC = 54398;
    private final static int BUFFER_SIZE = 1000;
    private Counter counter;


    public Writer(String host, int port, String prefix, int requests, int threadId, Counter counter) {
        this.host = host;
        this.port = port;
        this.prefix = prefix;
        this.requests = requests;
        this.threadId = threadId;
        this.myPort = MAGIC + threadId;
        this.counter = counter;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        boolean flag = true;
        while (flag) {
            flag = false;
            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                System.out.println("fail3");
                e.printStackTrace();
                flag = true;
            }
        }
        for (int i = 0; i < requests; ) {
            assert(socket != null);
            String message = prefix + threadId + "_" + i;
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName(host);
            } catch (UnknownHostException e) {
                assert(false);
                e.printStackTrace();
            }
            InetSocketAddress address = new InetSocketAddress(inetAddress, port);
            DatagramPacket outputPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, address);
            try {
                socket.send(outputPacket);
            } catch (IOException e) {
                assert(false);
                e.printStackTrace();
            }
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket inputPacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.setSoTimeout(10);
                socket.receive(inputPacket);
            } catch (IOException e) {
                continue;
            }

            String expectedResult = "Hello, " + message;
            String inputMessage = new String(inputPacket.getData(), inputPacket.getOffset(), inputPacket.getLength());
            if (!inputMessage.equals(expectedResult))
                continue;
            System.out.println(message + " " + inputMessage);
            i++;
        }
        socket.close();
        synchronized (counter) {
            counter.dec();
            if (counter.isZero())
                counter.notify();
        }
    }
}
