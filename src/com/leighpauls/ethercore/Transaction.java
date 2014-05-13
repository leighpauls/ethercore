package com.leighpauls.ethercore;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.leighpauls.ethercore.operation.EtherOperation;

import java.util.ArrayList;

/**
 * Transaction on the ether graph, moves the graph from one valid state to another according to the
 * user-space code.
 */
public class Transaction {
    private final Precedence mPrecedence;
    private final ImmutableList<EtherOperation> mOperations;

    public Transaction(Precedence precedence, ImmutableList<EtherOperation> operations) {
        mPrecedence = precedence;
        mOperations = operations;
    }

    public void apply(GraphDelegate delegate) {
        for (EtherOperation operation : mOperations) {
            operation.apply(delegate);
        }
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
                new Transaction(
                        source.local.mPrecedence,
                        ImmutableList.copyOf(xformedLocalOperations)),
                new Transaction(
                        source.remote.mPrecedence,
                        xformedRemoteOperations.build()));
    }

    public static class TransactionPair {
        public final Transaction local;
        public final Transaction remote;
        public TransactionPair(Transaction local, Transaction remote) {
            this.local = local;
            this.remote = remote;
        }
    }
}
