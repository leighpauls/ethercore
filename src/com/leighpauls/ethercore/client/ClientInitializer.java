package com.leighpauls.ethercore.client;

import com.leighpauls.ethercore.Precedence;
import com.leighpauls.ethercore.node.Node;
import com.leighpauls.ethercore.node.StructNode;

import java.util.Map;
import java.util.UUID;

/**
 * Data object holding all the things a client needs to initialize itself
 */
public class ClientInitializer {
    private final Precedence mPrecedence;
    private final StructNode mSeedNode;
    private final Map<UUID, Node> mNodes;
    private final ClientClock mClientClock;

    public ClientInitializer(
            Precedence precedence,
            StructNode seedNode,
            Map<UUID, Node> nodes,
            ClientClock clientClock) {
        mPrecedence = precedence;
        mSeedNode = seedNode;
        mNodes = nodes;
        mClientClock = clientClock;
    }

    public Precedence getPrecedence() {
        return mPrecedence;
    }

    public StructNode getSeedNode() {
        return mSeedNode;
    }

    public Map<UUID, Node> getNodes() {
        return mNodes;
    }

    public ClientClock getClientClock() {
        return mClientClock;
    }
}
