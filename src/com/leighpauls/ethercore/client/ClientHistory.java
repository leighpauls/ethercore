package com.leighpauls.ethercore.client;

import com.leighpauls.ethercore.Transaction;
import com.leighpauls.ethercore.except.EtherRuntimeException;

/**
 * Maintains the 2-dimensional history that a client needs to transform transactions down to the
 * most recent state
 */
public class ClientHistory {
    private boolean mPendingAck;
    /** The current state of the local copy of the graph */
    private ClientState mLastAppliedState;
    /** The latest state the the server is sure to know about */
    private ClientState mLatestRemoteEndState;

    public ClientHistory(ClientClock initialClock) {
        mPendingAck = false;
        mLastAppliedState = mLatestRemoteEndState = new ClientState(initialClock);
    }

    /**
     * Call when the server has acknowledged a local operation.
     * Incoming remote transactions will always be in the axis of the last acked operation
     * @param ackedLocalClock The local state that has been ack'ed by the server
     */
    public void applyServerAck(int ackedLocalClock) {
        if (ackedLocalClock != mLatestRemoteEndState.getClock().getLocalState() + 1) {
            throw new EtherRuntimeException("Got an ack for a local state other than the next one");
        }
        if (!mPendingAck) {
            throw new EtherRuntimeException("Got an ack when not expecting it");
        }
        mLatestRemoteEndState = mLatestRemoteEndState.getLocalTransition().getEndState();
        mPendingAck = false;
    }

    /**
     * Apply a remoteTransaction from the server to the local history.
     * Remote transactions must be sequential with each other in the plane of the last acked local
     * transaction.
     * @param remoteTransaction The remote remoteTransaction to apply
     * @return The transformed remoteTransaction to apply locally
     */
    public Transaction applyRemoteTransaction(ClientTransaction remoteTransaction) {
        if (!remoteTransaction.getSourceClock().equals(mLatestRemoteEndState.getClock())) {
            throw new EtherRuntimeException(
                    "Got a remote transaction from an unknown source state");
        }

        mLatestRemoteEndState.applyRemoteTransaction(remoteTransaction.getTransaction());
        ClientState transformationSource = mLatestRemoteEndState;
        mLatestRemoteEndState = mLatestRemoteEndState.getRemoteTransition().getEndState();

        // transform this change up to the most recently applied state
        while (transformationSource != mLastAppliedState) {
            ClientState.Transition localTransition = transformationSource.getLocalTransition();
            ClientState.Transition remoteTransition = transformationSource.getRemoteTransition();

            Transaction.TransactionPair transformedPair = Transaction.transform(
                    new Transaction.TransactionPair(
                            localTransition.getTransaction(),
                            remoteTransition.getTransaction())
            );

            // apply the transformed transactions
            localTransition.getEndState().applyRemoteTransaction(transformedPair.remote);
            remoteTransition.getEndState().applyLocalTransaction(transformedPair.local);

            // next iteration
            transformationSource = localTransition.getEndState();
        }

        mLastAppliedState = mLastAppliedState.getRemoteTransition().getEndState();
        return transformationSource.getRemoteTransition().getTransaction();
    }

    /**
     * Apply a transaction made locally.
     * Local transactions are applied serially in the axis of the last applied remote transaction.
     * @param transaction The local transaction to apply
     */
    public void applyLocalTransaction(Transaction transaction) {
        mLastAppliedState.applyLocalTransaction(transaction);
        mLastAppliedState = mLastAppliedState.getLocalTransition().getEndState();
    }

    /**
     * Retrieve the next local transaction to be send to the server. Note that this is mutable and
     * remove that transaction from the list of transactions that need to be sent
     * @return The next local transaction to send, or null if no local transaction can be sent
     */
    public ClientTransaction dequeueUnsentLocalTransaction() {
        if (mPendingAck || mLatestRemoteEndState == mLastAppliedState) {
            return null;
        }
        mPendingAck = true;

        return new ClientTransaction(
                mLatestRemoteEndState.getClock(),
                mLatestRemoteEndState.getLocalTransition().getTransaction());
    }
}
