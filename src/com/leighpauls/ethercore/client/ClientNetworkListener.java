package com.leighpauls.ethercore.client;

import com.leighpauls.ethercore.OperationDelegate;
import com.leighpauls.ethercore.Transaction;

/**
 * Interface for the {@link com.leighpauls.ethercore.client.EtherClient} to call when it needs to
 * send data to the server
 */
public interface ClientNetworkListener {

    /**
     * Called by the client to send a transaction
     * @param transaction
     */
    void sendTransaction(Transaction transaction);

    /**
     * Called by the client when initializing
     * @param operationDelegate
     * @return
     */
    ClientInitializer getInitializer(OperationDelegate operationDelegate);

    /**
     * Called when the client is ready to run
     * @param networkDelegate
     */
    void onClientReady(EtherClient.NetworkDelegate networkDelegate);
}
