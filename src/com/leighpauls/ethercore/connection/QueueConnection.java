package com.leighpauls.ethercore.connection;

import com.leighpauls.ethercore.OperationDelegate;
import com.leighpauls.ethercore.client.ClientInitializer;
import com.leighpauls.ethercore.client.ClientNetworkListener;
import com.leighpauls.ethercore.client.ClientTransaction;
import com.leighpauls.ethercore.client.EtherClient;
import com.leighpauls.ethercore.server.EtherServer;
import com.leighpauls.ethercore.server.PersistentNetworkClient;
import com.sun.tools.javac.util.Pair;

import java.util.*;

/**
 * An in-memory connection which permits control of how many messages can be sent at a time. For use
 * as a deterministic testing connection.
 */
public class QueueConnection {
    private final UUID mClientUUID;
    private ClientInitializer mInitializer;
    private EtherClient.NetworkDelegate mClientNetworkDelegate;
    private EtherServer.NetworkDelegate mServerNetworkDelegate;

    private final ArrayDeque<Runnable> mServerToClientQueue;
    private final ArrayDeque<Runnable> mClientToServerQueue;

    private final ClientListener mClientListener;
    private final ServerPersistentClient mServerPersistentClient;

    public QueueConnection(UUID clientUUID) {
        mClientUUID = clientUUID;
        mServerToClientQueue = new ArrayDeque<Runnable>();
        mClientToServerQueue = new ArrayDeque<Runnable>();

        mClientListener = new ClientListener();
        mServerPersistentClient = new ServerPersistentClient();
    }

    /**
     * Makes a new connection to the server, producing the client
     * @param server
     * @return
     */
    public static Pair<EtherClient, QueueConnection> connect(EtherServer server, UUID clientUUID) {
        QueueConnection connection = new QueueConnection(clientUUID);
        server.openClientConnection(connection.mServerPersistentClient);
        return new Pair<EtherClient, QueueConnection>(
                new EtherClient(connection.mClientListener),
                connection);
    }

    public int numPendingMessagesToServer() {
        return mClientToServerQueue.size();
    }

    public int numPendingMessagesToClient() {
        return mServerToClientQueue.size();
    }

    public void resolveMessageToServer() {
        mClientToServerQueue.removeFirst().run();
    }

    public void resolveMessageToClient() {
        mServerToClientQueue.removeFirst().run();
    }

    private class ClientListener implements ClientNetworkListener {
        @Override
        public void sendTransaction(final ClientTransaction transaction) {
            mClientToServerQueue.addLast(new Runnable() {
                @Override
                public void run() {
                    mServerNetworkDelegate.applyTransaction(transaction);
                }
            });
        }

        @Override
        public ClientInitializer getInitializer(OperationDelegate operationDelegate) {
            return mInitializer;
        }

        @Override
        public void onClientReady(EtherClient.NetworkDelegate networkDelegate) {
            mClientNetworkDelegate = networkDelegate;
        }
    }

    private class ServerPersistentClient implements PersistentNetworkClient {
        @Override
        public UUID getClientUUID() {
            return mClientUUID;
        }

        @Override
        public UUID getRootNodeUUID() {
            return EtherServer.GOD_NODE_UUID;
        }

        @Override
        public void initialize(
                ClientInitializer initializer,
                EtherServer.NetworkDelegate networkDelegate) {
            mInitializer = initializer;
            mServerNetworkDelegate = networkDelegate;
        }

        @Override
        public void sendAck(final int clientLocalClock) {
            mServerToClientQueue.addLast(new Runnable() {
                @Override
                public void run() {
                    mClientNetworkDelegate.deliverAck(clientLocalClock);
                }
            });
        }

        @Override
        public void sendTransaction(final ClientTransaction transaction) {
            mServerToClientQueue.addLast(new Runnable() {
                @Override
                public void run() {
                    mClientNetworkDelegate.deliverRemoteTransaction(transaction);
                }
            });
        }
    }
}
