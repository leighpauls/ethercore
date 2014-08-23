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

    public void applyRemoteTransaction(Transaction transaction, ClientState newEndState) {
        if (mRemoteTransition != null) {
            throw new EtherRuntimeException("Tried to overwrite a remote transaction");
        }
        if (!newEndState.getClock().equals(mClock.nextRemoteState())) {
            throw new EtherRuntimeException("Tried to skip a state");
        }
        mRemoteTransition = new Transition(transaction, newEndState);
    }

    public void applyLocalTransaction(Transaction transaction, ClientState newEndState) {
        if (mLocalTransition != null) {
            throw new EtherRuntimeException("Tried to overwrite a local transaction");
        }
        if (!newEndState.getClock().equals(mClock.nextLocalState())) {
            throw new EtherRuntimeException("Tried to skip a state");
        }
        mLocalTransition = new Transition(transaction, newEndState);
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
