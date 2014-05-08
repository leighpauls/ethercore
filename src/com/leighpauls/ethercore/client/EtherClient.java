package com.leighpauls.ethercore.client;

import com.google.common.collect.ImmutableList;
import com.leighpauls.ethercore.EtherEvent;
import com.leighpauls.ethercore.EtherTransactionInterface;
import com.leighpauls.ethercore.Precedence;
import com.leighpauls.ethercore.except.EtherRuntimeException;
import com.leighpauls.ethercore.except.MutationOutsideOfTransactionException;
import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.Node;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.operation.CreateList;
import com.leighpauls.ethercore.operation.CreateStruct;
import com.leighpauls.ethercore.operation.EtherOperation;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Interface that all clients use to access their ether graph
 */
public class EtherClient {
    private final Precedence mPrecedence;

    private final StructNode mSeedNode;
    private final HashMap<UUID, Node> mNodes;
    private final GraphDelegate mGraphDelegate;
    private final OperationDelegate mOperationDelegate;
    private final ClientHistory mHistory;

    private ImmutableList.Builder<EtherOperation> mPendingTransaction;

    public EtherClient(URI seedNodeURI, Precedence precedence) {
        mPrecedence = precedence;

        mPendingTransaction = null;
        mGraphDelegate = new GraphDelegate();
        mOperationDelegate = new OperationDelegate();

        // TODO: load the nodes and clock state from the seed URI
        mHistory = new ClientHistory(new ClientClock(0, 0));
        mNodes = new HashMap<UUID, Node>();
        mSeedNode = new StructNode(mGraphDelegate, UUID.randomUUID());
        mNodes.put(mSeedNode.getUUID(), mSeedNode);
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
        mGraphDelegate.applyOperation(operation);
        return (ListNode) mNodes.get(uuid);
    }

    /**
     * Create a new Struct Node
     * @return A newly created struct node
     */
    public StructNode makeStructNode() {
        UUID uuid = UUID.randomUUID();
        CreateStruct operation = new CreateStruct(uuid);
        mGraphDelegate.applyOperation(operation);
        return (StructNode) mNodes.get(uuid);
    }

    /**
     * All changes must be made from within the context of this call
     * @param transactionInterface The transaction to evaluate which will make changes to the graph
     */
    public synchronized void applyLocalTransaction(EtherTransactionInterface transactionInterface) {
        if (mPendingTransaction != null) {
            throw new EtherRuntimeException(
                    "Tried to start a transaction while one is already active");
        }

        mPendingTransaction = ImmutableList.builder();
        List<EtherEvent> events = transactionInterface.executeTransaction();

        // finish up this transaction
        ClientTransaction transaction = new ClientTransaction(
                mPrecedence,
                mHistory.getAppliedClock(),
                mPendingTransaction.build(),
                ImmutableList.copyOf(events));
        mHistory.applyLocalTransaction(transaction);
        mPendingTransaction = null;

        // TODO: transform any pending remote transactions over the new transaction
    }

    private synchronized void applyRemoteTransaction(ClientTransaction transaction) {
        // transform the remote transaction so that it's source matches the local clock

    }

    /**
     * Delegate exposing methods which the graph needs to talk back to the client
     */
    public class GraphDelegate {
        private GraphDelegate() {}

        public void applyOperation(EtherOperation operation) {
            if (mPendingTransaction == null) {
                throw new MutationOutsideOfTransactionException();
            }
            mPendingTransaction.add(operation);
            operation.apply(mOperationDelegate);
        }
    }

    /**
     * Delegate exposing methods needed by operations to apply changes to the graph
     */
    public class OperationDelegate {

        private OperationDelegate() {}
        public void addNode(UUID uuid, Node node) {
            mNodes.put(uuid, node);
        }

        public GraphDelegate getNodeDelegate() {
            return mGraphDelegate;
        }

        public Node getNode(UUID uuid) {
            return mNodes.get(uuid);
        }

        public ClientClock getClientClock() {
            return mHistory.getAppliedClock();
        }
    }
}