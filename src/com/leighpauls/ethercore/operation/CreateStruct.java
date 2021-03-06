package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.GraphDelegate;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.util.SerializationUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    public void apply(GraphDelegate delegate) {
        StructNode result = new StructNode(delegate.getOperationDelegate(), mUUID);
        delegate.addNode(mUUID, result);
    }

    @Override
    public EtherOperation transformOver(
            EtherOperation remoteOperation,
            boolean overrideRemote) {
        // creation never conflicts
        return this;
    }

    public CreateStruct(DataInputStream inputStream) throws IOException {
        mUUID = SerializationUtils.deserializeUUID(inputStream);
    }

    @Override
    public void serializeTypelessly(DataOutputStream outputStream) throws IOException {
        SerializationUtils.serializeUUID(mUUID, outputStream);
    }
}
