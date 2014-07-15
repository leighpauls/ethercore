package com.leighpauls.ethercore.connection;

/**
* Contract of message types for {@link com.leighpauls.ethercore.connection.SimpleTcpClient} and
 * {@link com.leighpauls.ethercore.connection.SimpleTcpPersistentClient}
*/
public enum SimpleTcpContract {
    INITIALIZER(1),
    CLIENT_READY(2),
    SERVER_TO_CLIENT_TRANSACTION(3),
    ACK(4),
    CLIENT_TO_SERVER_TRANSACTION(5);

    SimpleTcpContract(final int hardValue) {
        value = hardValue;
    }
    public final int value;
}
