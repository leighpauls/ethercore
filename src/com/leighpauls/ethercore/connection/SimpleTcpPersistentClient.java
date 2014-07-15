package com.leighpauls.ethercore.connection;

import com.google.common.util.concurrent.SettableFuture;
import com.leighpauls.ethercore.client.ClientInitializer;
import com.leighpauls.ethercore.client.ClientTransaction;
import com.leighpauls.ethercore.except.EtherRuntimeException;
import com.leighpauls.ethercore.server.EtherServer;
import com.leighpauls.ethercore.server.PersistentNetworkClient;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * The server-side endpoint which communicates with
 * {@link com.leighpauls.ethercore.connection.SimpleTcpClient}. This is a non-reconnecting interface
 * pretending to be persistent, so once it's disconnected it will act like an unresponsive client.
 */
public class SimpleTcpPersistentClient implements PersistentNetworkClient {
    private final Socket mSocket;
    private final DataOutputStream mOutputStream;
    private final UUID mClientUUID;

    private final SettableFuture<EtherServer.NetworkDelegate> mNetworkDelegateFuture;

    /** true iff the server has called initialize() */
    private boolean mIsInitialized;

    public SimpleTcpPersistentClient(Socket socket) throws IOException {
        mSocket = socket;
        mOutputStream = new DataOutputStream(mSocket.getOutputStream());
        mNetworkDelegateFuture = SettableFuture.create();

        mClientUUID = UUID.randomUUID();

        mIsInitialized = false;

        new Thread(new InputThread()).start();
    }

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
        mNetworkDelegateFuture.set(networkDelegate);

        synchronized (mOutputStream) {
            mIsInitialized = true;

            try {
                mOutputStream.writeInt(SimpleTcpContract.INITIALIZER.value);
                initializer.serialize(mOutputStream);
            } catch (IOException e) {
                throw new EtherRuntimeException("Failed to send initializer", e);
            }
        }
    }

    @Override
    public void sendAck(int clientLocalClock) {
        synchronized (mOutputStream) {
            if (!mIsInitialized) {
                throw new EtherRuntimeException("Sever sent ack before initializing");
            }
            try {
                mOutputStream.writeInt(SimpleTcpContract.ACK.value);
                mOutputStream.writeInt(clientLocalClock);
            } catch (IOException e) {
                throw new EtherRuntimeException("Failed to send ack", e);
            }
        }
    }

    @Override
    public void sendTransaction(ClientTransaction transaction) {
        synchronized (mOutputStream) {
            if (!mIsInitialized) {
                throw new EtherRuntimeException("Server sent transaction before initializing");
            }
            try {
                mOutputStream.writeInt(SimpleTcpContract.SERVER_TO_CLIENT_TRANSACTION.value);
                transaction.serialize(mOutputStream);
            } catch (IOException e) {
                throw new EtherRuntimeException("Failed to send transaction to client", e);
            }
        }
    }

    private class InputThread implements Runnable {
        @Override
        public void run() {
            try {
                DataInputStream inputStream = new DataInputStream(mSocket.getInputStream());

                while (mSocket.isConnected()) {
                    int messageType = inputStream.readInt();

                    if (messageType == SimpleTcpContract.CLIENT_READY.value) {
                        // client ready, this implementation does nothing with this information...
                    } else if (
                            messageType == SimpleTcpContract.CLIENT_TO_SERVER_TRANSACTION.value) {
                        try {
                            mNetworkDelegateFuture
                                    .get()
                                    .applyTransaction(new ClientTransaction(inputStream));
                        } catch (Exception e) {
                            throw new EtherRuntimeException("Failed to get network delegate", e);
                        }
                    } else {
                        throw new EtherRuntimeException(
                                "Unknown message type from client: "
                                        + Integer.toHexString(messageType));
                    }
                }
            } catch (IOException e) {
                throw new EtherRuntimeException("Unable to communicate with the client", e);
            }

        }
    }
}
