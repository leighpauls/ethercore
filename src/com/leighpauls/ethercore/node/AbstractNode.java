package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.client.OperationDelegate;

import java.util.UUID;

/**
 * Abstract implementation of {@link Node}
 */
public abstract class AbstractNode implements Node {
    private final OperationDelegate mOperationDelegate;
    private final UUID mUUID;

    protected AbstractNode(OperationDelegate operationDelegate, UUID uuid) {
        mOperationDelegate = operationDelegate;
        mUUID = uuid;
    }

    @Override
    public UUID getUUID() {
        return mUUID;
    }

    protected OperationDelegate getOperationDelegate() {
        return mOperationDelegate;
    }
}
