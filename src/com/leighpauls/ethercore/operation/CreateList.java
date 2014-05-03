package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.EtherClient;
import com.leighpauls.ethercore.node.ListNode;

import java.util.UUID;

/**
 * Operation to create a new {@link com.leighpauls.ethercore.node.ListNode}
 */
public class CreateList implements EtherOperation<ListNode> {
    private final UUID mUUID;

    public CreateList(UUID uuid) {
        mUUID = uuid;
    }

    @Override
    public ListNode apply(EtherClient.OperationDelegate delegate) {
        ListNode result = new ListNode(delegate.getNodeDelegate(), mUUID);
        delegate.addNode(mUUID, result);
        return result;
    }
}
