package com.leighpauls.ethercore.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.leighpauls.ethercore.*;
import com.leighpauls.ethercore.except.EtherRuntimeException;
import com.leighpauls.ethercore.except.MutationOutsideOfTransactionException;
import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.Node;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.operation.CreateList;
import com.leighpauls.ethercore.operation.CreateStruct;
import com.leighpauls.ethercore.operation.EtherOperation;

import java.util.HashMap;
import java.util.UUID;

/**
 * Interface that all clients use to access their ether graph
 */
public class EtherClient {
    private final ClientNetworkListener mNetworkListener;

    private final Precedence mPrecedence;
    private final StructNode mSeedNode;
    private final HashMap<UUID, Node> mNodes;

    private final OperationDelegate mOperationDelegate;
    private final GraphDelegate mGraphDelegate;

    private final ClientHistory mHistory;

    private ImmutableList.Builder<EtherOperation> mPendingTransaction;

    public EtherClient(ClientNetworkListener networkListener) {
        mNetworkListener = networkListener;
        mPendingTransaction = null;
        mOperationDelegate = new ClientOperationDelegate();
        mGraphDelegate = new ClientGraphDelegate();

        ClientInitializer initializer = networkListener.getInitializer(mOperationDelegate);
        mPrecedence = initializer.getPrecedence();

        // TODO: load the nodes and clock state from the seed URI
        mHistory = new ClientHistory(initializer.getClientClock());
        mNodes = Maps.newHashMap(initializer.getNodes());
        mSeedNode = initializer.getSeedNode();

        networkListener.onClientReady(new NetworkDelegate());
    }

    /**
     * Retrieve the seed node of this ether client instance
     * @return This client's seen node
     */
    public StructNode getSeedNode() {
        return mSeedNode;
    }

    /**
     * Create a new ListNode
     * @return A newly created list node
     */
    public ListNode makeListNode() {
        UUID uuid = UUID.randomUUID();
        CreateList operation = new CreateList(uuid);
        mOperationDelegate.applyOperation(operation);
        return (ListNode) mNodes.get(uuid);
    }

    /**
     * Create a new Struct Node
     * @return A newly created struct node
     */
    public StructNode makeStructNode() {
        UUID uuid = UUID.randomUUID();
        CreateStruct operation = new CreateStruct(uuid);
        mOperationDelegate.applyOperation(operation);
        return (StructNode) mNodes.get(uuid);
    }

    /**
     * All changes must be made from within the context of this call
     * @param transactionInterface The transaction to evaluate which will make changes to the graph
     */
    public void applyLocalTransaction(EtherTransactionInterface transactionInterface) {
        if (mPendingTransaction != null) {
            throw new EtherRuntimeException(
                    "Tried to start a transaction while one is already active");
        }

        mPendingTransaction = ImmutableList.builder();
        transactionInterface.executeTransaction();

        // finish up this transaction
        Transaction transaction = new Transaction(mPrecedence, mPendingTransaction.build());
        mHistory.applyLocalTransaction(transaction);
        mPendingTransaction = null;

        trySendingLocalTransaction();
    }

    private void trySendingLocalTransaction() {
        Transaction pendingTransaction = mHistory.dequeueUnsentLocalTransaction();
        if (pendingTransaction == null) {
            return;
        }
        mNetworkListener.sendTransaction(pendingTransaction);
    }

    private class ClientOperationDelegate implements OperationDelegate {
        private ClientOperationDelegate() {}

        @Override
        public void applyOperation(EtherOperation operation) {
            if (mPendingTransaction == null) {
                throw new MutationOutsideOfTransactionException();
            }
            mPendingTransaction.add(operation);
            operation.apply(mGraphDelegate);
        }
    }

    private class ClientGraphDelegate implements GraphDelegate {
        private ClientGraphDelegate() {}

        @Override
        public void addNode(UUID uuid, Node node) {
            mNodes.put(uuid, node);
        }

        @Override
        public OperationDelegate getOperationDelegate() {
            return mOperationDelegate;
        }

        @Override
        public Node getNode(UUID uuid) {
            return mNodes.get(uuid);
        }
    }

    public class NetworkDelegate {
        private NetworkDelegate() {}

        public void deliverAck(int localClock) {
            mHistory.applyServerAck(localClock);
            trySendingLocalTransaction();
        }

        public void deliverRemoteTransaction(ClientTransaction transaction) {
            Transaction transformedTransaction = mHistory.applyRemoteTransaction(transaction);
            transformedTransaction.apply(mGraphDelegate);
        }
    }
}
