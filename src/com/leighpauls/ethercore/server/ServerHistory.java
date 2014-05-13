package com.leighpauls.ethercore.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.leighpauls.ethercore.Transaction;
import com.leighpauls.ethercore.client.ClientClock;
import com.leighpauls.ethercore.client.ClientTransaction;
import com.leighpauls.ethercore.except.EtherRuntimeException;

import java.util.HashMap;
import java.util.UUID;

/**
 * Maintains a linear history of what the transactions applied to this server, as far back as needed
 * to transform client transactions to the present
 */
public class ServerHistory {
    private final HashMap<Integer, ServerState> mHistory;
    private ServerState mTipState;

    public ServerHistory() {
        // prime the history as empty, with a null initial state
        mHistory = Maps.newHashMap();
        mTipState = new ServerState(new ServerClock(ImmutableMap.<UUID, Integer>of(), 0));
        mHistory.put(0, mTipState);
    }

    /**
     * Apply the client's transaction to the server
     * @param clientTransaction Transaction from the client (in that client's context)
     * @param sourceClient The UUID of the client which sent the transaction
     * @return the transaction transformed up to the history's tip, ready to apply to the server's
     * copy of the graph
     */
    public Transaction applyClientTransaction(
            ClientTransaction clientTransaction,
            UUID sourceClient) {
        ClientClock clientClock = clientTransaction.getSourceClock();
        int sourceTotalState = clientClock.getLocalState() + clientClock.getRemoteState();
        if (!mHistory.containsKey(sourceTotalState)) {
            throw new EtherRuntimeException("Client transaction from unknown source state");
        }

        // transform the client transaction to the tip
        ServerState sourceState = mHistory.get(sourceTotalState);
        Transaction transformedTransaction = clientTransaction.getTransaction();
        while (sourceState != mTipState) {
            ServerState.Transition transition = sourceState.getTransition();
            Transaction.TransactionPair transformedPair = Transaction.transform(
                    new Transaction.TransactionPair(
                            transformedTransaction,
                            transition.getTransaction()));
            transformedTransaction = transformedPair.local;
            sourceState = transition.getEndState();
        }

        // apply the new transaction to the tip
        mTipState.applyTransaction(transformedTransaction, sourceClient);
        mTipState = mTipState.getTransition().getEndState();

        return transformedTransaction;
    }

    public ServerClock getTipClock() {
        return mTipState.getClock();
    }
}
