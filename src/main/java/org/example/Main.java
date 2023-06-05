package org.example;

import java.io.*;
import java.net.*;
import java.util.*;

public class Main {
    private static String hostIP = "164.164.164.189";
    private static Integer rank = 9;

    private static final int PORT = 1234;

    private static HashMap<String, Integer> localAddressAndRank = new HashMap<>();;
    private static HashMap<String, Integer> lastOptimalOne = new HashMap<>();;
    private static HashMap<String, Integer> mostUsableOne = new HashMap<>();;

    private static boolean changeServer = false;

    private static boolean isServer = false;

    private static HashMap<String, Integer> hosts;
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println("Hello world!");
        localAddressAndRank.put(hostIP, rank);

        Thread UDPEmitterThread = new Thread(() -> {
            try {
                UDPEmitter();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        UDPEmitterThread.start();

        Thread UDPListenerThread = new Thread(()->{
            try {
                UDPListener();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        UDPListenerThread.start();
    }


    public static void UDPEmitter() throws IOException, InterruptedException {
        hosts = new HashMap<>();
        hosts.put(hostIP, rank);

        ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream();
        ObjectOutputStream outputObjectStream = new ObjectOutputStream(outputByteStream);

        outputObjectStream.writeObject(hosts);
        outputObjectStream.flush();
        byte[] dataToSend = outputByteStream.toByteArray();

        InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
        int destinationPort = PORT;

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

    public static void UDPListener() throws IOException, ClassNotFoundException {
        hosts.put(hostIP, rank);

        byte[] receiveDataBuffer = new byte[1024];

        int UDPReceiverPort = PORT;

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

            mostUsableOne.put(mostUsableHost, highiestRank);

            if(lastOptimalOne.isEmpty()){
                lastOptimalOne.put(mostUsableHost, highiestRank);
            }

            if(!lastOptimalOne.equals(mostUsableOne)){
                lastOptimalOne = mostUsableOne;

                System.out.println("LAST OPTIMAL ONE CHANGED");
            }

            // As server
            if(isServer && !mostUsableOne.equals(localAddressAndRank)){
                // Change to client
                isServer = false;

                System.out.println("SERVER TO CLIENT DONE");
            }

            // As client
            if(!isServer && mostUsableOne.equals(localAddressAndRank)){
                // Change to server
                isServer = true;
                changeServer = false;

                System.out.println("CLIENT TO SERVER DONE");
            }

            if(!isServer && !lastOptimalOne.equals(mostUsableOne)){
                // Change socket
                changeServer = true;
                System.out.println("CHANGE SERVER AS A CLIENT DONE");
            }
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