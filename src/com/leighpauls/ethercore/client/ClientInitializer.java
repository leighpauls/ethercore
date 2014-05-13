package com.leighpauls.ethercore.client;

import com.google.common.collect.ImmutableSet;
import com.leighpauls.ethercore.Precedence;
import com.leighpauls.ethercore.node.NodeData;
import com.leighpauls.ethercore.node.StructNode;

import java.util.UUID;

/**
 * Data object holding all the things a client needs to initialize itself
 */
public class ClientInitializer {
    private final Precedence mPrecedence;
    private final UUID mSeedNodeUUID;
    private final ImmutableSet<NodeData> mNodes;
    private final ClientClock mClientClock;

    public ClientInitializer(
            Precedence precedence,
            UUID seedNodeUUID,
            ImmutableSet<NodeData> nodes,
            ClientClock clientClock) {
        mPrecedence = precedence;
        mSeedNodeUUID = seedNodeUUID;
        mNodes = nodes;
        mClientClock = clientClock;
    }

    public Precedence getPrecedence() {
        return mPrecedence;
    }

    public UUID getSeedNodeUUID() {
        return mSeedNodeUUID;
    }

    public ImmutableSet<NodeData> getNodes() {
        return mNodes;
    }

    public ClientClock getClientClock() {
        return mClientClock;
    }
}
