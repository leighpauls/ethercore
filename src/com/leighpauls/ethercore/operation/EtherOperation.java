package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.client.EtherClient;

/**
 * All operations (things that can mutate the tree) implement this
 */
public interface EtherOperation {
    /**
     * Apply the operation
     * @param delegate delegate of the EtherClient to apply the operation to
     */
    void apply(EtherClient.OperationDelegate delegate);

    EtherOperation transformOver(EtherOperation remoteOperation, boolean overrideRemote);
}
