package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.EtherClient;
import com.leighpauls.ethercore.except.MutationOutsideOfTransactionException;

import java.util.UUID;

/**
 * Abstract implementation of {@link Node}
 */
public abstract class AbstractNode implements Node {
    private final EtherClient.EtherClientDelegate mEtherClientDelegate;
    private final UUID mUUID;

    protected AbstractNode(EtherClient.EtherClientDelegate etherClientDelegate, UUID uuid) {
        mEtherClientDelegate = etherClientDelegate;
        mUUID = uuid;
    }

    @Override
    public UUID getUUID() {
        return mUUID;
    }

    /**
     * Throws {@link com.leighpauls.ethercore.except.MutationOutsideOfTransactionException} if the node is
     * not safely mutable.
     */
    public void verifyNodeIsMutable() {
        if (!mEtherClientDelegate.isInTranaction()) {
            throw new MutationOutsideOfTransactionException();
        }
        // TODO: throw an exception if the transaction belongs to another thread
    }
}
