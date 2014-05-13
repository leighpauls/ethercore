package com.leighpauls.ethercore.client;

import com.leighpauls.ethercore.Transaction;

/**
 * Encapsulation of a transaction as understood by the client
 */
public class ClientTransaction {
    private final ClientClock mSourceClock;
    private final Transaction mTransaction;

    public ClientTransaction(ClientClock sourceClock, Transaction transaction) {
        mSourceClock = sourceClock;
        mTransaction = transaction;
    }

    public ClientClock getSourceClock() {
        return mSourceClock;
    }

    public Transaction getTransaction() {
        return mTransaction;
    }
}
