package com.leighpauls.ethercore.server;

import com.leighpauls.ethercore.client.ClientInitializer;
import com.leighpauls.ethercore.client.ClientTransaction;
import com.leighpauls.ethercore.server.EtherServer;

import java.util.UUID;

/**
 * Interface through which the server communicates with clients
 */
public interface PersistentNetworkClient {
    UUID getClientUUID();
    UUID getRootNodeUUID();

    void initialize(ClientInitializer initializer, EtherServer.NetworkDelegate networkDelegate);

    void sendAck(int clientLocalClock);
    void sendTransaction(ClientTransaction transaction);
}
