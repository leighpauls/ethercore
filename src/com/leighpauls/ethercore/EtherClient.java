package com.leighpauls.ethercore;

import com.google.common.collect.ImmutableList;
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
import java.util.UUID;

/**
 * Interface that all clients use to access their ether graph
 */
public class EtherClient {
    private final StructNode mSeedNode;
    private final HashMap<UUID, Node> mNodes;
    private final GraphDelegate mGraphDelegate;
    private final OperationDelegate mOperationDelegate;

    private ImmutableList.Builder<EtherOperation<?>> mPendingTransaction;

    public EtherClient(URI seedNodeURI) {
        mPendingTransaction = null;
        mGraphDelegate = new GraphDelegate();
        mOperationDelegate = new OperationDelegate();

        // TODO: load the nodes from the seed
        mNodes = new HashMap<UUID, Node>();
        mSeedNode = new StructNode(mGraphDelegate, UUID.randomUUID());
        mNodes.put(mSeedNode.getUUID(), mSeedNode);
    }

    /**
     * Retrieve the seed node of this ether client instance
     * @return
     */
    public StructNode getSeedNode() {
        return mSeedNode;
    }

    /**
     * Create a new ListNode
     * @return A newly created list node
     */
    public ListNode makeListNode() {
        CreateList operation = new CreateList(UUID.randomUUID());
        return mGraphDelegate.applyOperation(operation);
    }

    /**
     * Create a new Struct Node
     * @return A newly created struct node
     */
    public StructNode makeStructNode() {
        CreateStruct operation = new CreateStruct(UUID.randomUUID());
        return mGraphDelegate.applyOperation(operation);
    }

    /**
     * All changes must be made from within the context of this call
     * @param transactionInterface The transaction to evaluate which will make changes to the graph
     */
    public void applyTransaction(EtherTransactionInterface transactionInterface) {
        if (mPendingTransaction != null) {
            throw new EtherRuntimeException(
                    "Tried to start a transaction while one is already active");
        }


        mPendingTransaction = ImmutableList.builder();
        transactionInterface.executeTransaction();

        // TODO: apply pending remote transactions, send the new local transaction
        System.out.println("Executed transaction: " + mPendingTransaction.build());

        mPendingTransaction = null;
    }

    /**
     * Delegate exposing methods which the graph needs to talk back to the client
     */
    public class GraphDelegate {
        private GraphDelegate() {}

        public <T extends Node> T applyOperation(EtherOperation<T> operation) {
            if (mPendingTransaction == null) {
                throw new MutationOutsideOfTransactionException();
            }
            mPendingTransaction.add(operation);
            return operation.apply(mOperationDelegate);
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
    }
}
