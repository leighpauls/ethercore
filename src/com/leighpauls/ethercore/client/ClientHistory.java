package com.leighpauls.ethercore.client;

import com.leighpauls.ethercore.except.EtherRuntimeException;

/**
 * Maintains the 2-dimensional history that a client needs to transform transactions down to the
 * most recent state
 */
public class ClientHistory {
    private ClientState mNewestState;
    private ClientState mLastAppliedState;
    private ClientState mLatestRemoteEndState;

    public ClientHistory(ClientClock initialClock) {
        mNewestState = mLastAppliedState = mLatestRemoteEndState = new ClientState(initialClock);
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
        mLatestRemoteEndState = mLatestRemoteEndState.getLocalTransition().getEndState();
    }

    /**
     * Apply a transaction from the server to the local history.
     * Remote transactions must be sequential with eachother in the plane of the last acked local
     * transaction.
     * @param transaction The remote transaction to apply
     */
    public void applyRemoteTransaction(ClientTransaction transaction) {
        if (!transaction.getSourceClock().equals(mLatestRemoteEndState.getClock())) {
            throw new EtherRuntimeException(
                    "Got a remote transaction from an unknown source state");
        }

        mLatestRemoteEndState.applyRemoteTransaction(transaction);
        ClientState transformationSource = mLatestRemoteEndState;
        mLatestRemoteEndState = mLatestRemoteEndState.getRemoteTransition().getEndState();

        // TODO: this could all be done in an alternate thread
        // transform this change up to the most recently applied state
        while (transformationSource != mNewestState) {
            ClientState.Transition localTransition = transformationSource.getLocalTransition();
            ClientState.Transition remoteTransition = transformationSource.getRemoteTransition();

            ClientTransaction.TransactionPair transformedPair = ClientTransaction.transform(
                    new ClientTransaction.TransactionPair(
                            localTransition.getTransaction(),
                            remoteTransition.getTransaction()));

            // apply the transformed transactions
            localTransition.getEndState().applyRemoteTransaction(transformedPair.remote);
            remoteTransition.getEndState().applyLocalTransaction(transformedPair.local);

            // next iteration
            transformationSource = localTransition.getEndState();
        }

        mNewestState = mNewestState.getRemoteTransition().getEndState();
    }

    /**
     * Apply a transaction made locally.
     * Local transactions are applied serially in the axis of the last applied remote transaction.
     * @param transaction The local transaction to apply
     */
    public void applyLocalTransaction(ClientTransaction transaction) {
        if (!transaction.getSourceClock().equals(mLastAppliedState.getClock())) {
            throw new EtherRuntimeException(
                    "Applying a local transition that doesn't come from the current state");
        }

        mLastAppliedState.applyLocalTransaction(transaction);
        ClientState transformationSource = mLastAppliedState;
        mLastAppliedState = mLatestRemoteEndState.getLocalTransition().getEndState();

        // TODO: this could all be done in an alternate thread
        // transform this check up to the latest received remote transition's state
        while (transformationSource != mNewestState) {
            ClientState.Transition localTransition = transformationSource.getLocalTransition();
            ClientState.Transition remoteTransition = transformationSource.getRemoteTransition();

            ClientTransaction.TransactionPair transformedPair = ClientTransaction.transform(
                    new ClientTransaction.TransactionPair(
                            localTransition.getTransaction(),
                            remoteTransition.getTransaction()));

            // apply the transformed transactions
            localTransition.getEndState().applyRemoteTransaction(transformedPair.remote);
            remoteTransition.getEndState().applyLocalTransaction(transformedPair.local);

            // next iteration
            transformationSource = remoteTransition.getEndState();
        }

        mNewestState = mNewestState.getLocalTransition().getEndState();
    }

    public ClientClock getAppliedClock() {
        return mLastAppliedState.getClock();
    }
}
