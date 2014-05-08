package com.leighpauls.ethercore.client;

/**
 * Interface for the {@link com.leighpauls.ethercore.client.EtherClient} to call when it needs to
 * send data to the server
 */
public interface ClientNetworkListener {

    /**
     * Called by the client to send a transaction
     * @param transaction
     */
    void sendTransaction(ClientTransaction transaction);

    /**
     * Called by the client when initializing
     * @param graphDelegate
     * @return
     */
    ClientInitializer getInitializer(EtherClient.GraphDelegate graphDelegate);

    /**
     * Called when the client is ready to run
     * @param networkDelegate
     */
    void onClientReady(EtherClient.NetworkDelegate networkDelegate);
}
