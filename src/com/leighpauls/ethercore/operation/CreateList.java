package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.GraphDelegate;
import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.util.SerializationUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Operation to create a new {@link com.leighpauls.ethercore.node.ListNode}
 */
public class CreateList implements EtherOperation {
    private final UUID mUUID;

    public CreateList(UUID uuid) {
        mUUID = uuid;
    }

    @Override
    public void apply(GraphDelegate delegate) {
        ListNode result = new ListNode(delegate.getOperationDelegate(), mUUID);
        delegate.addNode(mUUID, result);
    }

    @Override
    public EtherOperation transformOver(
            EtherOperation remoteOperation,
            boolean overrideRemote) {
        // creation never conflicts
        return this;
    }

    public CreateList(DataInputStream inputStream) throws IOException {
        mUUID = SerializationUtils.deserializeUUID(inputStream);
    }

    @Override
    public void serializeTypelessly(DataOutputStream outputStream) throws IOException {
        SerializationUtils.serializeUUID(mUUID, outputStream);
    }
}
