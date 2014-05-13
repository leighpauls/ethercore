package com.leighpauls.ethercore.server;

import com.leighpauls.ethercore.Transaction;
import com.leighpauls.ethercore.except.EtherRuntimeException;

import java.util.UUID;

/**
 * Represents one state in the server's history
 */
public class ServerState {
    private final ServerClock mClock;
    private Transition mTransition;

    public ServerState(ServerClock clock) {
        mClock = clock;
        mTransition = null;
    }

    public Transition getTransition() {
        return mTransition;
    }

    public void applyTransaction(Transaction transaction, UUID sourceClient) {
        if (mTransition != null) {
            throw new EtherRuntimeException("Tried to overwrite a transaction in history");
        }
        mTransition = new Transition(transaction, new ServerState(mClock.nextState(sourceClient)));
    }

    public ServerClock getClock() {
        return mClock;
    }

    public static class Transition {
        private final Transaction mTransaction;
        private final ServerState mEndState;

        public Transition(Transaction transaction, ServerState endState) {
            mTransaction = transaction;
            mEndState = endState;
        }

        public Transaction getTransaction() {
            return mTransaction;
        }

        public ServerState getEndState() {
            return mEndState;
        }
    }
}
