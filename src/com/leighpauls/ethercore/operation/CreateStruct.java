package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.EtherClient;
import com.leighpauls.ethercore.node.StructNode;

import java.util.UUID;

/**
 * Operation to create a new {@link com.leighpauls.ethercore.node.StructNode}
 */
public class CreateStruct implements EtherOperation<StructNode> {
    private final UUID mUUID;

    public CreateStruct(UUID uuid) {
        mUUID = uuid;
    }

    @Override
    public StructNode apply(EtherClient.OperationDelegate delegate) {
        StructNode result = new StructNode(delegate.getNodeDelegate(), mUUID);
        delegate.addNode(mUUID, result);
        return result;
    }
}
