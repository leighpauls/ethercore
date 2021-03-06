package com.leighpauls.ethercore.server;

import com.google.common.collect.Maps;
import com.leighpauls.ethercore.*;
import com.leighpauls.ethercore.client.ClientClock;
import com.leighpauls.ethercore.client.ClientInitializer;
import com.leighpauls.ethercore.client.ClientTransaction;
import com.leighpauls.ethercore.except.EtherRuntimeException;
import com.leighpauls.ethercore.node.Node;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.operation.EtherOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Object that contains the entire server state, and initiates communication with clients
 */
public class EtherServer {
    private final HashMap<UUID, Node> mNodes;
    private final HashMap<UUID, PersistentNetworkClient> mClients;
    private final ServerHistory mHistory;
    private final OperationDelegate mOperationDelegate;
    private final GraphDelegate mGraphDelegate;
    private int mNextPrecedence;
    private int totalClock;

    public static final UUID GOD_NODE_UUID = new UUID(0, 0);

    public EtherServer() {
        mNodes = Maps.newHashMap();
        mClients = Maps.newHashMap();
        mHistory = new ServerHistory();
        mOperationDelegate = new ServerOperationDelegate();
        mGraphDelegate = new ServerGraphDelegate();
        mNextPrecedence = 0;
        totalClock = 0;

        // initialize with a god node
        mNodes.put(GOD_NODE_UUID, new StructNode(mOperationDelegate, GOD_NODE_UUID));
    }

    /**
     * Opens a new client connection (one that has never existed before)
     * @param client
     */
    public void openClientConnection(PersistentNetworkClient client) {
        UUID clientUUID = client.getClientUUID();
        // TODO: handle re-opening past connections
        mClients.put(clientUUID, client);

        Precedence precedence = new Precedence(mNextPrecedence);
        mNextPrecedence += 1;

        UUID rootNodeUUID = client.getRootNodeUUID();
        if (!mNodes.containsKey(rootNodeUUID)) {
            throw new EtherRuntimeException("Asked for a root node which doesn't exist");
        }

        client.initialize(
                new ClientInitializer(
                        precedence,
                        rootNodeUUID,
                        NodeSerializer.serializeNodes(mNodes.values()),
                        new ClientClock(0, totalClock)),
                new NetworkDelegate(clientUUID));
    }

    public Node getNode(UUID nodeUuid) {
        return mNodes.get(nodeUuid);
    }

    // TODO: expose only for testing
    public HashMap<UUID, Node> getNodes() {
        return mNodes;
    }

    public class NetworkDelegate {
        private final UUID mClientUUID;
        private NetworkDelegate (UUID clientUUID) {
            mClientUUID = clientUUID;
        }

        public void applyTransaction(ClientTransaction transaction) {
            // transform the transaction down to the current state and apply the transaction
            ServerClock tipClock = mHistory.getTipClock();
            Transaction transformedTransaction =
                    mHistory.applyClientTransaction(transaction, mClientUUID);
            transformedTransaction.apply(mGraphDelegate);

            // tell the other clients about the transaction
            for (Map.Entry<UUID, PersistentNetworkClient> client : mClients.entrySet()) {
                UUID clientUUID = client.getKey();
                if (clientUUID.equals(mClientUUID)) {
                    // ack back to the originating client
                    client.getValue().sendAck(tipClock.forClient(clientUUID).getLocalState() + 1);
                    continue;
                }
                // send the transaction to other clients
                client.getValue().sendTransaction(
                        new ClientTransaction(
                                tipClock.forClient(clientUUID),
                                transformedTransaction));
            }
        }
    }

    private static class ServerOperationDelegate implements OperationDelegate {
        @Override
        public void applyOperation(EtherOperation operation) {
            throw new EtherRuntimeException("Tried to apply an operation on the server");
        }
    }

    private class ServerGraphDelegate implements GraphDelegate {
        @Override
        public void addNode(UUID uuid, Node node) {
            mNodes.put(uuid, node);
        }

        @Override
        public OperationDelegate getOperationDelegate() {
            return mOperationDelegate;
        }

        @Override
        public Node getNode(UUID uuid) {
            return mNodes.get(uuid);
        }
    }
}
