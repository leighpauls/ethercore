package com.leighpauls.ethercore;

import com.leighpauls.ethercore.client.EtherClient;
import com.leighpauls.ethercore.connection.SimpleTcpClient;
import com.leighpauls.ethercore.connection.SimpleTcpServer;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.server.EtherServer;
import com.leighpauls.ethercore.value.*;

import java.io.IOException;

/**
 * Simple main class for the {@link com.leighpauls.ethercore.connection.SimpleTcpClient}, so that
 * operations can be tested asynchronously
 */
public class SimpleTcpTest {
    public static void main(String[] args) {
        System.out.println("Starting");
        EtherServer server = new EtherServer();
        System.out.println("made server");
        String host = "localhost";
        SimpleTcpServer tcpServer = new SimpleTcpServer(server, host, 0, 5);
        System.out.println("made tcp server");
        tcpServer.startListening();
        System.out.println("started listening");
        int port = tcpServer.getListeningPort();
        System.out.println("listening on port: " + port);

        final EtherClient clientA;
        try {
            clientA = SimpleTcpClient.connect(host, port);
            System.out.println("made clientA");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        clientA.applyLocalTransaction(new EtherTransactionInterface() {
            @Override
            public void executeTransaction() {
                StructNode seedNode = clientA.getSeedNode();
                seedNode.put("hello", new StringValue("world!"));
                System.out.println("applied transaction to A");
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("slept");

        try {
            EtherClient clientB = SimpleTcpClient.connect(host, port);
            System.out.println("made B");
            GraphCrawlingPrinter.printGraph(clientB.getSeedNode(), 5);
            System.out.println("printed");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
