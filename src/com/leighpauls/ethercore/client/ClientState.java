package com.leighpauls.ethercore.client;

import com.leighpauls.ethercore.Transaction;
import com.leighpauls.ethercore.except.EtherRuntimeException;

/**
 * Represents one state in the client's history
 */
public class ClientState {
    private final ClientClock mClock;
    private Transition mLocalTransition;
    private Transition mRemoteTransition;

    public ClientState(ClientClock clock) {
        mClock = clock;
        mLocalTransition = null;
        mRemoteTransition = null;
    }

    public ClientClock getClock() {
        return mClock;
    }

    public Transition getLocalTransition() {
        return mLocalTransition;
    }

    public Transition getRemoteTransition() {
        return mRemoteTransition;
    }

    public void applyRemoteTransaction(Transaction transaction) {
        if (mRemoteTransition != null) {
            throw new EtherRuntimeException("Tried to overwrite a remote transaction");
        }
        mRemoteTransition = new Transition(transaction, new ClientState(mClock.nextRemoteState()));
    }

    public void applyLocalTransaction(Transaction transaction) {
        if (mLocalTransition != null) {
            throw new EtherRuntimeException("Tried to overwrite a local transaction");
        }
        mLocalTransition = new Transition(transaction, new ClientState(mClock.nextLocalState()));
    }

    public static class Transition {
        private final Transaction mTransaction;
        private final ClientState mEndState;

        private Transition(Transaction transaction, ClientState endState) {
            mTransaction = transaction;
            mEndState = endState;
        }

        public Transaction getTransaction() {
            return mTransaction;
        }

        public ClientState getEndState() {
            return mEndState;
        }
    }
}
