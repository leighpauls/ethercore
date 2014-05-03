package com.leighpauls.ethercore;

import com.leighpauls.ethercore.except.EtherRuntimeException;
import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.Node;
import com.leighpauls.ethercore.node.StructNode;

import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

/**
 * Interface that all clients use to access their ether graph
 */
public class EtherClient {
    private final StructNode mSeedNode;
    private final HashMap<UUID, Node> mNodes;
    private final EtherClientDelegate mDelegate;

    private boolean mInTransaction;

    public EtherClient(URI seedNodeURI) {
        mInTransaction = false;
        mDelegate = new EtherClientDelegate();

        // TODO: load the nodes from the seed
        mNodes = new HashMap<UUID, Node>();
        mSeedNode = makeStructNode();
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
        ListNode result = new ListNode(mDelegate, UUID.randomUUID());
        mNodes.put(result.getUUID(), result);
        return result;
    }

    /**
     * Create a new Stuct Node
     * @return A newly created struct node
     */
    public StructNode makeStructNode() {
        StructNode result = new StructNode(mDelegate, UUID.randomUUID());
        mNodes.put(result.getUUID(), result);
        return result;
    }

    public void applyTransaction(EtherTransactionInterface transactionInterface) {
        if (mInTransaction) {
            throw new EtherRuntimeException(
                    "Tried to start a transaction while one is already active");
        }
        mInTransaction = true;
        // TODO: start recording mutations
        transactionInterface.executeTransaction();
        // TODO: stop recording mutations, finalize the transactions
        mInTransaction = false;
        // TODO: apply pending remote transactions, send the new local transaction
    }

    public class EtherClientDelegate {
        private EtherClientDelegate() {}
        public boolean isInTranaction() {
            return mInTransaction;
        }
    }
}
