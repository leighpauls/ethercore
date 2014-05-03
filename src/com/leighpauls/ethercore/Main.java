package com.leighpauls.ethercore;

import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.value.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final EtherClient client;
        try {
            client = new EtherClient(new URI("http://localhost:8888"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        final StructNode seedNode = client.getSeedNode();

        client.applyTransaction(new EtherTransactionInterface() {
            @Override
            public List<EtherEvent> executeTransaction() {
                ListNode listNode = client.makeListNode();
                seedNode.put("my_list", listNode.getReference());
                seedNode.put("my_int", new IntegerValue(55));

                listNode.insert(0, new StringValue("hello"));
                listNode.insert(1, new StringValue("World!"));

                return new ArrayList<EtherEvent>();
            }
        });

        GraphCrawlingPrinter.printGraph(seedNode, 5);
    }
}
