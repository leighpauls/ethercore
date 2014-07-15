package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.GraphDelegate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Empty operation, used when a conflict causes a transform to produce a redundant operation
 */
public class NoOp implements EtherOperation {
    public NoOp() {

    }

    @Override
    public void apply(GraphDelegate delegate) {}

    @Override
    public EtherOperation transformOver(EtherOperation remoteOperation, boolean overrideRemote) {
        return this;
    }

    public NoOp(DataInputStream inputStream) {

    }

    @Override
    public void serializeTypelessly(DataOutputStream outputStream) throws IOException {
    }
}
