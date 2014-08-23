package com.leighpauls.ethercore.test;

import com.leighpauls.ethercore.EtherTransactionInterface;
import com.leighpauls.ethercore.GraphCrawlingPrinter;
import com.leighpauls.ethercore.client.EtherClient;
import com.leighpauls.ethercore.connection.QueueConnection;
import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.server.EtherServer;
import com.leighpauls.ethercore.value.*;
import com.sun.tools.javac.util.Pair;

import java.util.UUID;


/**
 * For testing specific transactions
 */
public class TransactionProbe {
    public static void main(String[] args) {
        EtherServer server = new EtherServer();
        Pair<EtherClient, QueueConnection> connectionPairA =
                QueueConnection.connect(server, UUID.randomUUID());
        Pair<EtherClient, QueueConnection> connectionPairB =
                QueueConnection.connect(server, UUID.randomUUID());

        final EtherClient clientA = connectionPairA.fst;
        clientA.applyLocalTransaction(new EtherTransactionInterface() {
            @Override
            public void executeTransaction() {
                clientA.getSeedNode().put("a", new StringValue("1"));
            }
        });
        clientA.applyLocalTransaction(new EtherTransactionInterface() {
            @Override
            public void executeTransaction() {
                clientA.getSeedNode().put("a", new StringValue("2"));
            }
        });

        final EtherClient clientB = connectionPairB.fst;
        clientB.applyLocalTransaction(new EtherTransactionInterface() {
            @Override
            public void executeTransaction() {
                clientB.getSeedNode().put("b", new StringValue("1"));
            }
        });
        clientB.applyLocalTransaction(new EtherTransactionInterface() {
            @Override
            public void executeTransaction() {
                clientB.getSeedNode().put("b", new StringValue("2"));
            }
        });

        QueueConnection connectionA = connectionPairA.snd;
        QueueConnection connectionB = connectionPairB.snd;
        printStatus("Before resolving", clientA, connectionA, clientB, connectionB);

        connectionA.resolveMessageToServer();
        connectionA.resolveMessageToClient();
        connectionB.resolveMessageToClient();
        printStatus("One resolved", clientA, connectionA, clientB, connectionB);

        connectionB.resolveMessageToServer();
        connectionA.resolveMessageToClient();
        printStatus("Resolved back", clientA, connectionA, clientB, connectionB);
        connectionB.resolveMessageToClient();
    }

    private static void printStatus(
            String message,
            EtherClient clientA,
            QueueConnection connectionA,
            EtherClient clientB,
            QueueConnection connectionB) {
        System.out.println(message + ":");
        GraphCrawlingPrinter.printGraph(clientA.getSeedNode(), 5);
        System.out.println(
                "Up: " + connectionA.numPendingMessagesToServer()
                        + " Down: " + connectionA.numPendingMessagesToClient());
        GraphCrawlingPrinter.printGraph(clientB.getSeedNode(), 5);
        System.out.println(
                "Up: " + connectionB.numPendingMessagesToServer()
                        + " Down: " + connectionB.numPendingMessagesToClient());
        System.out.println();
    }
}
