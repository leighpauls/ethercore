package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.GraphDelegate;

/**
 * Holds the content of a node for serialization
 */
public interface NodeData {
    public Node recreate(GraphDelegate operationDelegate);
}
