package ru.ifmo.ctddev.belonogov.helloUDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by vanya on 17.05.15.
 */
public class Listener implements Runnable {
    private final static int BUFFER_SIZE = 1000;
    private DatagramSocket socket;

    public Listener(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        for (;;) {
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
            String newMessage = "Hello, " + message;
            byte[] outBuffer = newMessage.getBytes();
            DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length, packet.getSocketAddress());
            try {
                socket.send(outPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
