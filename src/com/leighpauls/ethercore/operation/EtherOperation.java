package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.GraphDelegate;

/**
 * All operations (things that can mutate the tree) implement this
 */
public interface EtherOperation {
    /**
     * Apply the operation
     * @param delegate delegate of the EtherClient to apply the operation to
     */
    void apply(GraphDelegate delegate);

    EtherOperation transformOver(EtherOperation remoteOperation, boolean overrideRemote);
}
