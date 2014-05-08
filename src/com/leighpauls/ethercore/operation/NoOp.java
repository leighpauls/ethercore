package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.client.EtherClient;

/**
 * Empty operation, used when a conflict causes a transform to produce a redundant operation
 */
public class NoOp implements EtherOperation {

    @Override
    public void apply(EtherClient.OperationDelegate delegate) {}

    @Override
    public EtherOperation transformOver(EtherOperation remoteOperation, boolean overrideRemote) {
        return this;
    }
}
