package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.GraphDelegate;

/**
 * Empty operation, used when a conflict causes a transform to produce a redundant operation
 */
public class NoOp implements EtherOperation {

    @Override
    public void apply(GraphDelegate delegate) {}

    @Override
    public EtherOperation transformOver(EtherOperation remoteOperation, boolean overrideRemote) {
        return this;
    }
}
