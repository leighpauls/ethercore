package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.client.EtherClient;
import com.leighpauls.ethercore.node.StructNode;

import java.util.UUID;

/**
 * Operation to create a new {@link com.leighpauls.ethercore.node.StructNode}
 */
public class CreateStruct implements EtherOperation {
    private final UUID mUUID;

    public CreateStruct(UUID uuid) {
        mUUID = uuid;
    }

    @Override
    public void apply(EtherClient.OperationDelegate delegate) {
        StructNode result = new StructNode(delegate.getNodeDelegate(), mUUID);
        delegate.addNode(mUUID, result);
    }

    @Override
    public EtherOperation transformOver(
            EtherOperation remoteOperation,
            boolean overrideRemote) {
        // creation never conflicts
        return this;
    }
}
