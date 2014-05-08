package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.client.EtherClient;

import java.util.UUID;

/**
 * Abstract implementation of {@link Node}
 */
public abstract class AbstractNode implements Node {
    private final EtherClient.GraphDelegate mGraphDelegate;
    private final UUID mUUID;

    protected AbstractNode(EtherClient.GraphDelegate graphDelegate, UUID uuid) {
        mGraphDelegate = graphDelegate;
        mUUID = uuid;
    }

    @Override
    public UUID getUUID() {
        return mUUID;
    }

    protected EtherClient.GraphDelegate getGraphDelegate() {
        return mGraphDelegate;
    }
}
