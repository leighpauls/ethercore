package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.client.EtherClient;
import com.leighpauls.ethercore.node.ListNode;

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
    public void apply(EtherClient.OperationDelegate delegate) {
        ListNode result = new ListNode(delegate.getNodeDelegate(), mUUID);
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
