package com.leighpauls.ethercore;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.leighpauls.ethercore.client.*;
import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.Node;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.operation.EtherOperation;
import com.leighpauls.ethercore.server.EtherServer;
import com.leighpauls.ethercore.value.*;

import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        EtherServer server = new EtherServer();
        final EtherClient client = ImmediateConnection.connect(server);
        final EtherClient clientB = ImmediateConnection.connect(server);

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

        final StructNode seedNodeB = clientB.getSeedNode();
        clientB.applyLocalTransaction(new EtherTransactionInterface() {
            @Override
            public void executeTransaction() {
                ListNode listNodeB = seedNodeB.get("my_list").asListReference();
                listNodeB.insert(1, new IntegerValue(12));

                System.out.println("Before B's commit:");
                GraphCrawlingPrinter.printGraph(seedNode, 5);
                GraphCrawlingPrinter.printGraph(seedNodeB, 5);
            }
        });

        System.out.println("After B's commit:");
        GraphCrawlingPrinter.printGraph(seedNode, 5);
        GraphCrawlingPrinter.printGraph(seedNodeB, 5);
    }

}
