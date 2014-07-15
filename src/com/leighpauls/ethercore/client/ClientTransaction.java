package com.leighpauls.ethercore.client;

import com.leighpauls.ethercore.Transaction;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    public ClientTransaction(DataInputStream inputStream) throws IOException {
        mSourceClock = new ClientClock(inputStream);
        mTransaction = new Transaction(inputStream);
    }

    public void serialize(DataOutputStream outputStream) throws IOException {
        mSourceClock.serialize(outputStream);
        mTransaction.serialize(outputStream);
    }

    public ClientClock getSourceClock() {
        return mSourceClock;
    }

    public Transaction getTransaction() {
        return mTransaction;
    }

}
