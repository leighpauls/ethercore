package com.leighpauls.ethercore.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.leighpauls.ethercore.EtherEvent;
import com.leighpauls.ethercore.Precedence;
import com.leighpauls.ethercore.except.EtherRuntimeException;
import com.leighpauls.ethercore.operation.EtherOperation;

import java.util.ArrayList;

/**
 * Transaction on the ether graph, moves the graph from one valid state to another according to the
 * user-space code.
 */
public class ClientTransaction {
    private final Precedence mPrecedence;
    private final ClientClock mSourceClock;
    private final ImmutableList<EtherOperation> mOperations;
    private final ImmutableList<EtherEvent> mEvents;

    public ClientTransaction(
            Precedence precedence,
            ClientClock sourceClock,
            ImmutableList<EtherOperation> operations,
            ImmutableList<EtherEvent> events) {
        mPrecedence = precedence;
        mSourceClock = sourceClock;
        mOperations = operations;
        mEvents = events;
    }

    void apply(EtherClient.OperationDelegate delegate) {
        if (!mSourceClock.equals(delegate.getClientClock())) {
            throw new EtherRuntimeException(
                    "Tried to apply a transaction on something other than it's source state");
        }
        for (EtherOperation operation : mOperations) {
            operation.apply(delegate);
        }
    }

    public ClientClock getSourceClock() {
        return mSourceClock;
    }

    public static TransactionPair transform(TransactionPair source) {
        ArrayList<EtherOperation> xformedLocalOperations =
                Lists.newArrayList(source.local.mOperations);
        ImmutableList.Builder<EtherOperation> xformedRemoteOperations =
                ImmutableList.<EtherOperation>builder();

        boolean localOverrides = source.local.mPrecedence.overrides(source.remote.mPrecedence);

        for (EtherOperation remoteOperation : source.remote.mOperations) {
            for (int i = 0; i < xformedLocalOperations.size(); i++) {
                EtherOperation localOperation = xformedLocalOperations.get(i);
                xformedLocalOperations.set(
                        i,
                        localOperation.transformOver(remoteOperation, localOverrides));
                remoteOperation = remoteOperation.transformOver(localOperation, !localOverrides);
            }
            xformedRemoteOperations.add(remoteOperation);
        }

        return new TransactionPair(
                new ClientTransaction(
                        source.local.mPrecedence,
                        source.local.getSourceClock().nextRemoteState(),
                        ImmutableList.copyOf(xformedLocalOperations),
                        source.local.mEvents),
                new ClientTransaction(
                        source.remote.mPrecedence,
                        source.local.getSourceClock().nextLocalState(),
                        xformedRemoteOperations.build(),
                        source.remote.mEvents));
    }

    public static class TransactionPair {
        public final ClientTransaction local;
        public final ClientTransaction remote;
        public TransactionPair(ClientTransaction local, ClientTransaction remote) {
            this.local = local;
            this.remote = remote;
        }
    }
}
