package ru.ifmo.rain.chizhikov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class HelloUDPClient implements HelloClient {

    public static void main(String[] args) {
        try {
            String host = args[0];
            int port = Integer.parseInt(Objects.requireNonNull(args[1]));
            String prefix = args[2];
            int numberOfThreads = Integer.parseInt(Objects.requireNonNull(args[3]));
            int numberOfRequests = Integer.parseInt(Objects.requireNonNull(args[4]));

            new HelloUDPClient().run(host, port, prefix, numberOfThreads, numberOfRequests);
        } catch (Exception e) {
            System.err.println("Usage: <host> <prefix> <port> <number of threads> <number of requests> (Arguments must be non-null).");
        }
    }

    @Override
    public void run(String host, int port, String prefix, int numberOfThreads, int numberOfRequests) {
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);

        ExecutorService workers = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; ++i) {
            final int threadId = i;

            workers.submit(new Thread(() -> {
                try (DatagramSocket datagramSocket = new DatagramSocket()) {

                    byte[] dataResponse = new byte[datagramSocket.getReceiveBufferSize()];
                    DatagramPacket response = new DatagramPacket(dataResponse, datagramSocket.getReceiveBufferSize());

                    for (int requestId = 0; requestId < numberOfRequests; ++requestId) {
                        String request = String.format("%s%d_%d", prefix, threadId, requestId);
                        byte[] dataRequest = request.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket packet = new DatagramPacket(dataRequest, request.length(), socketAddress);

                        datagramSocket.setSoTimeout(500);
                        while (true) {
                            try {
                                datagramSocket.send(packet);
                                datagramSocket.receive(response);
                            } catch (SocketTimeoutException e) {
                                System.err.println("ERROR: Timeout: " + e);
                            } catch (PortUnreachableException e) {
                                System.err.println("ERROR: Socket connection destination is currently unavailable: " + e);
                            } catch (IOException e) {
                                System.err.println("ERROR: I/O exception while sending: " + e);
                            }

                            String result = new String(response.getData(), response.getOffset(), response.getLength(), StandardCharsets.UTF_8);
                            if (result.matches(".*" + Pattern.quote(request) + "(|\\p{Space}.*)")) {
                                System.out.println(result);
                                break;
                            }
                        }
                    }
                } catch (SocketException e) {
                    System.err.println("ERROR: the socket could not be opened," +
                            "or the socket could not bind to the specified local port." + e);
                }
            }));
        }
        workers.shutdown();

        try {
            workers.awaitTermination(numberOfThreads * numberOfRequests, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
        }
    }
}
