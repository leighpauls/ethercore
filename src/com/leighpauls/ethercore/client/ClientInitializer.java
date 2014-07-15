package com.leighpauls.ethercore.client;

import com.google.common.collect.ImmutableSet;
import com.leighpauls.ethercore.Precedence;
import com.leighpauls.ethercore.node.NodeData;
import com.leighpauls.ethercore.node.NodeDataSerializer;
import com.leighpauls.ethercore.util.SerializationUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public void serialize(DataOutputStream output) throws IOException {
        mPrecedence.serialize(output);
        SerializationUtils.serializeUUID(mSeedNodeUUID, output);

        output.writeInt(mNodes.size());
        for (NodeData node : mNodes) {
            NodeDataSerializer.serialize(node, output);
        }

        mClientClock.serialize(output);
    }

    public ClientInitializer(DataInputStream input) throws IOException {
        mPrecedence = new Precedence(input);
        mSeedNodeUUID = SerializationUtils.deserializeUUID(input);


        int numNodes = input.readInt();
        ImmutableSet.Builder<NodeData> builder = ImmutableSet.builder();
        for (int i = 0; i < numNodes; ++i) {
            builder.add(NodeDataSerializer.deserialize(input));
        }
        mNodes = builder.build();

        mClientClock = new ClientClock(input);
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
