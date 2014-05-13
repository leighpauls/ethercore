package com.leighpauls.ethercore;

import com.leighpauls.ethercore.client.OperationDelegate;
import com.leighpauls.ethercore.node.Node;

import java.util.UUID;

/**
 * Interface which exposes methods needed to navigate and modify the graph
 */
public interface GraphDelegate {
    void addNode(UUID uuid, Node node);

    OperationDelegate getOperationDelegate();

    Node getNode(UUID uuid);
}
