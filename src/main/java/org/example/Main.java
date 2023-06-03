package org.example;

import java.io.*;
import java.net.*;
import java.util.*;

public class Main {
    private static String hostIP = "155.155.155.200";
    private static Integer rank = 5;

    private static HashMap<String, Integer> hosts;
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println("Hello world!");

        Thread serverThread = new Thread(() -> {
            try {
                runAsServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        serverThread.start();

        Thread clientThread = new Thread(()->{
            try {
                runAsClient();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        clientThread.start();
    }


    public static void runAsServer() throws IOException, InterruptedException {
        hosts = new HashMap<>();
        hosts.put(hostIP, rank);

        ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream();
        ObjectOutputStream outputObjectStream = new ObjectOutputStream(outputByteStream);

        outputObjectStream.writeObject(hosts);
        outputObjectStream.flush();
        byte[] dataToSend = outputByteStream.toByteArray();

        InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
        int destinationPort = 1234;



        // Socket creation
        DatagramSocket UDPSocket = new DatagramSocket();
        DatagramPacket UDPPacket = new DatagramPacket(
                dataToSend,
                dataToSend.length,
                broadcastAddress,
                destinationPort
        );

        while(true){
            UDPSocket.send(UDPPacket);
            System.out.println("Packet sent. ");
            Thread.sleep(2000);
        }
    }

    public static void runAsClient() throws IOException, ClassNotFoundException {
        hosts.put(hostIP, rank);

        byte[] receiveDataBuffer = new byte[1024];

        int UDPReceiverPort = 1234;

        DatagramSocket UDPSocket = new DatagramSocket(UDPReceiverPort);
        DatagramPacket UDPPacket = new DatagramPacket(receiveDataBuffer, receiveDataBuffer.length);


        while(true){
            UDPSocket.receive(UDPPacket);

            byte[] receivedBytes = UDPPacket.getData();

            // Deserialize into a hashmap
            ByteArrayInputStream inputByteStream = new ByteArrayInputStream(receivedBytes);
            ObjectInputStream inputObjectStream = new ObjectInputStream(inputByteStream);
            HashMap<String, Integer> receivedHashMap = (HashMap<String, Integer>) inputObjectStream.readObject();


            System.out.println("Before merge");
            System.out.println(hosts);

            hosts.putAll(receivedHashMap);

            System.out.println("Merged HashMap");
            System.out.println(hosts);


            ArrayList<Map.Entry<String, Integer>> entryList = hashMapToArrayList(hosts);

            System.out.println("Received data:");
            String mostUsableHost = "";
            int highiestRank = 0;
            for (Map.Entry<String, Integer> entry : entryList) {
                System.out.println(entry.getKey() + ": " + entry.getValue());

                if(entry.getValue() > highiestRank){
                    highiestRank = entry.getValue();
                    mostUsableHost = entry.getKey();
                }
            }

            System.out.println("Most usable one");
            System.out.println(mostUsableHost);

        }
    }

    public static ArrayList<Map.Entry<String, Integer>> hashMapToArrayList(HashMap<String, Integer> receivedHashMap){
        // HashMap sorting
        ArrayList<Map.Entry<String, Integer>> entryList = new ArrayList<>(receivedHashMap.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                // Comparar los valores de las entradas en orden descendente
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });

        return entryList;
    }
}