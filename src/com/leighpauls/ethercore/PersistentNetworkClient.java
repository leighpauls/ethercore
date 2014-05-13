package com.leighpauls.ethercore;

import com.leighpauls.ethercore.client.ClientInitializer;
import com.leighpauls.ethercore.client.ClientTransaction;

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
