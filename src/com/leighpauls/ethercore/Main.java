package com.leighpauls.ethercore;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.leighpauls.ethercore.client.*;
import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.Node;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.operation.EtherOperation;
import com.leighpauls.ethercore.value.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        final EtherClient.NetworkDelegate[] delegate = new EtherClient.NetworkDelegate[1];
        ClientNetworkListener networkListener = new ClientNetworkListener() {
            @Override
            public void sendTransaction(ClientTransaction transaction) {
                System.out.println("Transaction: " + transaction);
            }

            @Override
            public ClientInitializer getInitializer(EtherClient.GraphDelegate graphDelegate) {
                StructNode seedNode = new StructNode(graphDelegate, UUID.randomUUID());
                return new ClientInitializer(
                        new Precedence(1),
                        seedNode,
                        ImmutableMap.<UUID, Node>of(seedNode.getUUID(), seedNode),
                        new ClientClock(0, 0));
            }

            @Override
            public void onClientReady(EtherClient.NetworkDelegate networkDelegate) {
                delegate[0] = networkDelegate;
            }
        };
        final EtherClient client = new EtherClient(networkListener);

        final StructNode seedNode = client.getSeedNode();

        client.applyLocalTransaction(new EtherTransactionInterface() {
            @Override
            public List<EtherEvent> executeTransaction() {
                ListNode listNode = client.makeListNode();
                seedNode.put("my_list", listNode.getReference());
                seedNode.put("my_int", new IntegerValue(55));
                seedNode.put("world", new StringValue("Hello!"));

                listNode.insert(0, new StringValue("hello"));
                listNode.insert(1, new StringValue("World!"));

                return new ArrayList<EtherEvent>();
            }
        });

        delegate[0].deliverRemoteTransaction(
                new ClientTransaction(
                        new Precedence(2),
                        new ClientClock(0, 0),
                        ImmutableList.<EtherOperation>of(
                                new StructNode.Put(
                                        seedNode.getUUID(),
                                        "world",
                                        new StringValue("Hola!"))),
                        ImmutableList.<EtherEvent>of()));

        GraphCrawlingPrinter.printGraph(seedNode, 5);
    }
}
