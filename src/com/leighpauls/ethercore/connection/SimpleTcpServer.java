package com.leighpauls.ethercore.connection;

import com.google.common.util.concurrent.SettableFuture;
import com.leighpauls.ethercore.except.EtherRuntimeException;
import com.leighpauls.ethercore.server.EtherServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

/**
 * Listens for incoming connections from {@link com.leighpauls.ethercore.connection.SimpleTcpClient}
 */
public class SimpleTcpServer {

    private final EtherServer mServer;
    private final String mHost;
    private final int mPort;
    private final int mBacklog;

    private final SettableFuture<Integer> mListeningPortFuture;
    private boolean mIsRunning;

    public SimpleTcpServer(EtherServer server, String host, int port, int backlog) {
        mServer = server;
        mHost = host;
        mPort = port;
        mBacklog = backlog;

        mListeningPortFuture = SettableFuture.create();

        mIsRunning = false;
    }

    public void startListening() {
        if (mIsRunning) {
            throw new EtherRuntimeException("Tried to start the server more than once");
        }
        mIsRunning = true;
        new Thread(new AcceptThread()).start();
    }

    /**
     * Blocks until the server is listening
     * @return The port that this server is listening on
     */
    public int getListeningPort() {
        try {
            return mListeningPortFuture.get();
        } catch (Exception e) {
            throw new EtherRuntimeException("Unable to get the listening port", e);
        }
    }

    /**
     * Thread for accepting new socket connections.
     */
    private class AcceptThread implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket;
            try {
                serverSocket = new ServerSocket(
                        mPort,
                        mBacklog,
                        InetAddress.getByName(mHost));
            } catch (IOException e) {
                throw new EtherRuntimeException("Failed to create the server socket", e);
            }

            mListeningPortFuture.set(serverSocket.getLocalPort());

            while (true) {
                Socket acceptedSocket;
                try {
                    acceptedSocket = serverSocket.accept();
                    mServer.openClientConnection(new SimpleTcpPersistentClient(acceptedSocket));
                } catch (IOException e) {
                    throw new EtherRuntimeException("Failed to accept a socket", e);
                }
            }
        }
    }
}
