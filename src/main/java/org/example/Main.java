package org.example;

import java.io.IOException;
import java.net.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello world!");
        boolean isServer = true;

        if(isServer){
            runAsServer();
        } else {
            runAsClient();
        }

    }

    public static void runAsServer() throws IOException, InterruptedException {
        String mostUsableHost = "155.155.155.155";

        DatagramSocket UDPBroadcastSocket = new DatagramSocket();
        UDPBroadcastSocket.setBroadcast(true);
        byte[] buffer = mostUsableHost.getBytes();
        InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
        int clientPort = 12345;
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, clientPort);

        while(true){
            UDPBroadcastSocket.send(packet);
            System.out.println("Sent");
            Thread.sleep(2000);
        }
    }

    public static void runAsClient() throws IOException {
        DatagramSocket socket = new DatagramSocket(12345);

        byte[] receiveBuffer = new byte[1024];

        while(true){
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Message from server: " + receivedMessage);
        }

    }
}