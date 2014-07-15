package com.leighpauls.ethercore.connection;

import com.leighpauls.ethercore.OperationDelegate;
import com.leighpauls.ethercore.client.ClientInitializer;
import com.leighpauls.ethercore.client.ClientNetworkListener;
import com.leighpauls.ethercore.client.ClientTransaction;
import com.leighpauls.ethercore.client.EtherClient;
import com.leighpauls.ethercore.server.EtherServer;
import com.leighpauls.ethercore.server.PersistentNetworkClient;

import java.util.UUID;

/**
 * Trivial testing class which connects the server to the client in the same thread
 */
public class ImmediateConnection {

    private final ServerConnection mServerConnection;
    private final ClientConnection mClientConnection;
    private final UUID mClientUUID;
    private ClientInitializer mInitializer;
    private EtherServer.NetworkDelegate mServerNetworkDelegate;
    private EtherClient.NetworkDelegate mClientNetworkDelegate;

    private ImmediateConnection() {
        mServerConnection = new ServerConnection();
        mClientConnection = new ClientConnection();
        mClientUUID = UUID.randomUUID();
    }

    public static EtherClient connect(EtherServer server) {
        ImmediateConnection connection = new ImmediateConnection();
        server.openClientConnection(connection.mServerConnection);
        return new EtherClient(connection.mClientConnection);
    }

    private class ServerConnection implements PersistentNetworkClient {
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
        public void sendAck(int clientLocalClock) {
            mClientNetworkDelegate.deliverAck(clientLocalClock);
        }

        @Override
        public void sendTransaction(ClientTransaction transaction) {
            mClientNetworkDelegate.deliverRemoteTransaction(transaction);
        }
    }

    private class ClientConnection implements ClientNetworkListener {
        @Override
        public void sendTransaction(ClientTransaction transaction) {
            mServerNetworkDelegate.applyTransaction(transaction);
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
}
