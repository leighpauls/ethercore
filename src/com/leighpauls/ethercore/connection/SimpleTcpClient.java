package com.leighpauls.ethercore.connection;

import com.google.common.util.concurrent.SettableFuture;
import com.leighpauls.ethercore.OperationDelegate;
import com.leighpauls.ethercore.client.*;
import com.leighpauls.ethercore.except.EtherRuntimeException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

/**
 * Connection type which uses a TCP client-server connection using the naive assumption that the
 * TCP socket will remain open indefinitely
 * Warning: This is a development implementation that assumes a perfect network!!!!
 */
public class SimpleTcpClient implements ClientNetworkListener {

    private final Socket mSocket;
    private final DataOutputStream mOutputStream;
    private final SettableFuture<ClientInitializer> mClientInitializerFuture;
    private final SettableFuture<EtherClient.NetworkDelegate> mNetworkDelegateFuture;

    public static EtherClient connect(String serverHost, int port) throws IOException {
        Socket socket = new Socket(serverHost, port);
        SimpleTcpClient simpleTcpClient = new SimpleTcpClient(socket);
        return new EtherClient(simpleTcpClient);
    }

    private SimpleTcpClient(Socket socket) throws IOException {
        mSocket = socket;
        mOutputStream = new DataOutputStream(socket.getOutputStream());
        mClientInitializerFuture = SettableFuture.create();
        mNetworkDelegateFuture = SettableFuture.create();

        // start the input thread
        new Thread(new InputThread()).start();
    }

    private void recieveAck(DataInputStream input) throws IOException {
        int locallyAckedClock = input.readInt();
        try {
            mNetworkDelegateFuture.get().deliverAck(locallyAckedClock);
        } catch (InterruptedException e) {
            throw new EtherRuntimeException(e);
        } catch (ExecutionException e) {
            throw new EtherRuntimeException(e);
        }
    }

    private void receiveTransaction(DataInputStream input) throws IOException {
        try {
            mNetworkDelegateFuture.get().deliverRemoteTransaction(new ClientTransaction(input));
        } catch (InterruptedException e) {
            throw new EtherRuntimeException(e);
        } catch (ExecutionException e) {
            throw new EtherRuntimeException(e);
        }
    }

    @Override
    public void sendTransaction(ClientTransaction transaction) {
        synchronized (mOutputStream) {
            try {
                mOutputStream.writeInt(SimpleTcpContract.CLIENT_TO_SERVER_TRANSACTION.value);
                transaction.serialize(mOutputStream);
            } catch (IOException e) {
                throw new EtherRuntimeException(e);
            }
        }
    }

    @Override
    public ClientInitializer getInitializer(OperationDelegate operationDelegate) {
        try {
            return mClientInitializerFuture.get();
        } catch (Exception e) {
            throw new EtherRuntimeException(e);
        }
    }

    @Override
    public void onClientReady(EtherClient.NetworkDelegate networkDelegate) {
        mNetworkDelegateFuture.set(networkDelegate);
        synchronized (mOutputStream) {
            try {
                mOutputStream.writeInt(SimpleTcpContract.CLIENT_READY.value);
            } catch (IOException e) {
                throw new EtherRuntimeException(e);
            }
        }
    }

    private class InputThread implements Runnable {
        @Override
        public void run() {
            try {
                DataInputStream input = new DataInputStream(mSocket.getInputStream());

                while (mSocket.isConnected()) {
                    int messageType = input.readInt();

                    if (messageType == SimpleTcpContract.INITIALIZER.value) {
                        mClientInitializerFuture.set(new ClientInitializer(input));
                    } else if (
                            messageType == SimpleTcpContract.SERVER_TO_CLIENT_TRANSACTION.value) {
                        receiveTransaction(input);
                    } else if (messageType == SimpleTcpContract.ACK.value) {
                        recieveAck(input);
                    } else {
                        throw new EtherRuntimeException(
                                "Invalid message type from the server: "
                                        + Integer.toHexString(messageType));
                    }
                }
            } catch (IOException e) {
                throw new EtherRuntimeException("Unable to communicate with the server: ", e);
            }
        }
    }
}
