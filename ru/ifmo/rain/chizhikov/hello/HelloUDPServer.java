package ru.ifmo.rain.chizhikov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.*;

public class HelloUDPServer implements HelloServer {
    private ExecutorService workers;
    private ExecutorService receiver;
    private DatagramSocket socket;
    private boolean closed = true;

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(Objects.requireNonNull(args[0]));
            int numberOfThreads = Integer.parseInt(Objects.requireNonNull(args[1]));

            new HelloUDPServer().start(port, numberOfThreads);
        } catch (Exception e) {
            System.err.println("Usage: <port> <number of threads> (Arguments must be non-null).");
        }
    }

    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);

            receiver = Executors.newSingleThreadExecutor();
            workers = new ThreadPoolExecutor(threads, threads,
                    1, TimeUnit.MINUTES,
                    new ArrayBlockingQueue<>(100000), new ThreadPoolExecutor.DiscardPolicy());

            closed = false;
        } catch (SocketException e) {
            System.err.println("ERROR: Unable to create socket connected to port " + port);
            return;
        }

        receiver.submit(() -> {
            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    byte[] data = new byte[socket.getReceiveBufferSize()];
                    DatagramPacket packet = new DatagramPacket(data, data.length);

                    socket.receive(packet);
                    workers.submit(() -> {
                        String responseText = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);

                        byte[] responseData = new byte[0];
                        DatagramPacket response = new DatagramPacket(responseData, 0, packet.getSocketAddress());

                        response.setData(("Hello, " + responseText).getBytes(StandardCharsets.UTF_8));

                        try {
                            socket.send(response);
                        } catch (IOException e) {
                            if (!closed) {
                                System.err.println("ERROR: I/O exception while sending: " + e.getMessage());
                            }
                        }
                    });
                } catch (IOException e) {
                    if (!closed) {
                        System.err.println("ERROR: I/O exception with datagram: " + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void close() {
        closed = true;
        socket.close();
        receiver.shutdownNow();
        workers.shutdownNow();

        try {
            workers.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }
}
