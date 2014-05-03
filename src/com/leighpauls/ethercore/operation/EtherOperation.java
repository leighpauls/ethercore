package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.EtherClient;
import com.leighpauls.ethercore.node.Node;

/**
 * All operations (things that can mutate the tree) implement this
 */
public interface EtherOperation <T extends Node> {
    /**
     * Apply the operation
     * @param delegate delegate of the EtherClient to apply the operation to
     * @return The target node
     */
    T apply(EtherClient.OperationDelegate delegate);
}
