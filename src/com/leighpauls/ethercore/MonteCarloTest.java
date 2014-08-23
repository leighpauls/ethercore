package com.leighpauls.ethercore;

import com.leighpauls.ethercore.client.EtherClient;
import com.leighpauls.ethercore.connection.QueueConnection;
import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.server.EtherServer;
import com.leighpauls.ethercore.value.*;
import com.sun.tools.javac.util.Pair;

import java.util.UUID;

/**
 * Tests random valid combinations of transactions for consistency
 */
public class MonteCarloTest {

    public static void main(String[] args) {
        EtherServer server = new EtherServer();
        Pair<EtherClient, QueueConnection> connectionPair =
                QueueConnection.connect(server, UUID.randomUUID());

        final EtherClient client = connectionPair.fst;
        final StructNode seedNode = client.getSeedNode();

        client.applyLocalTransaction(new EtherTransactionInterface() {
            @Override
            public void executeTransaction() {
                ListNode listNode = client.makeListNode();
                seedNode.put("my_list", listNode.getReference());
                seedNode.put("my_int", new IntegerValue(55));
                seedNode.put("world", new StringValue("Hello!"));

                listNode.insert(0, new StringValue("hello"));
                listNode.insert(1, new StringValue("World!"));
            }
        });

        Pair<EtherClient, QueueConnection> connectionPairB =
                QueueConnection.connect(server, UUID.randomUUID());

        QueueConnection[] connections = {connectionPair.snd, connectionPairB.snd};

        resolveAllPendingMessages(connections);

        EtherClient clientB = connectionPairB.fst;
        final StructNode seedNodeB = clientB.getSeedNode();
        clientB.applyLocalTransaction(new EtherTransactionInterface() {
            @Override
            public void executeTransaction() {
                ListNode listNodeB = seedNodeB.get("my_list").asListReference();
                listNodeB.insert(1, new IntegerValue(12));
            }
        });

        System.out.println("Before resolving:");
        GraphCrawlingPrinter.printGraph(seedNode, 5);
        GraphCrawlingPrinter.printGraph(seedNodeB, 5);

        resolveAllPendingMessages(connections);
        System.out.println("After resolving:");
        GraphCrawlingPrinter.printGraph(seedNode, 5);
        GraphCrawlingPrinter.printGraph(seedNodeB, 5);

    }

    private static void resolveAllPendingMessages(QueueConnection[] connections) {
        boolean done;
        do {
            done = true;
            for (QueueConnection connection : connections) {
                if (connection.numPendingMessagesToServer() > 0) {
                    connection.resolveMessageToServer();
                    done = false;
                } else if (connection.numPendingMessagesToClient() > 0) {
                    connection.resolveMessageToClient();
                    done = false;
                }
            }
        } while (!done);
    }

}
