package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.client.EtherClient;
import com.leighpauls.ethercore.operation.EtherOperation;
import com.leighpauls.ethercore.operation.NoOp;
import com.leighpauls.ethercore.value.AbstractValue;
import com.leighpauls.ethercore.value.Value;

import java.util.ArrayList;
import java.util.UUID;

/**
 * List-based EtherCore Node
 */
public class ListNode extends AbstractNode {
    private final ArrayList<Value> mValues;
    private final ListReferenceValue mSelfReference;

    public ListNode(EtherClient.GraphDelegate graphDelegate, UUID uuid) {
        super(graphDelegate, uuid);
        mValues = new ArrayList<Value>();
        mSelfReference = new ListReferenceValue();
    }

    public Value get(int index) {
        return mValues.get(index);
    }

    public int size() {
        return mValues.size();
    }

    public ListReferenceValue getReference() {
        return mSelfReference;
    }

    // mutation operations, protected by transaction restrictions
    public void insert(int index, Value value) {
        Insert operation = new Insert(getUUID(), index, value);
        getGraphDelegate().applyOperation(operation);
    }
    public void remove(int index) {
        Remove operation = new Remove(getUUID(), index);
        getGraphDelegate().applyOperation(operation);
    }

    public static class Insert implements EtherOperation {
        private final UUID mTargetUUID;
        private final int mIndex;
        private final Value mValue;

        public Insert(UUID uuid, int index, Value value) {
            mTargetUUID = uuid;
            mIndex = index;
            mValue = value;
        }

        @Override
        public void apply(EtherClient.OperationDelegate delegate) {
            ListNode target = (ListNode) delegate.getNode(mTargetUUID);
            target.mValues.add(mIndex, mValue);
        }

        @Override
        public EtherOperation transformOver(
                EtherOperation remoteOperation,
                boolean overrideRemote) {
            if (remoteOperation instanceof Insert) {
                return xformOverInsert((Insert) remoteOperation, overrideRemote);
            } else if (remoteOperation instanceof Remove) {
                return xformOverRemove((Remove) remoteOperation);
            }
            // no conflcit possible
            return this;
        }

        private EtherOperation xformOverInsert(Insert remoteOperation, boolean overrideRemote) {
            if (!remoteOperation.mTargetUUID.equals(remoteOperation.mTargetUUID)) {
                return this;
            }
            if (remoteOperation.mIndex < mIndex
                    || (remoteOperation.mIndex == mIndex && !overrideRemote)) {
                // I've been shifted back one
                return new Insert(mTargetUUID, mIndex + 1, mValue);
            }
            // I don't get moved
            return this;
        }

        private EtherOperation xformOverRemove(Remove remoteOperation) {
            if (!remoteOperation.mTargetUUID.equals(mTargetUUID)) {
                return this;
            }
            if (remoteOperation.mIndex < mIndex) {
                // I've been shifted forward one
                return new Insert(mTargetUUID, mIndex - 1, mValue);
            }
            // I don't get moved
            return this;
        }
    }

    private static class Remove implements EtherOperation {
        private final UUID mTargetUUID;
        private final int mIndex;

        private Remove(UUID uuid, int index) {
            mTargetUUID = uuid;
            mIndex = index;
        }

        @Override
        public void apply(EtherClient.OperationDelegate delegate) {
            ListNode target = (ListNode) delegate.getNode(mTargetUUID);
            target.mValues.remove(mIndex);
        }

        @Override
        public EtherOperation transformOver(
                EtherOperation remoteOperation,
                boolean overrideRemote) {
            if (remoteOperation instanceof Insert) {
                return xformOverInsert((Insert) remoteOperation);
            } else if (remoteOperation instanceof Remove) {
                return xformOverRemove((Remove) remoteOperation, overrideRemote);
            }
            // no conflict possible
            return this;
        }

        private EtherOperation xformOverInsert(Insert remoteOperation) {
            if (!remoteOperation.mTargetUUID.equals(mTargetUUID)) {
                return this;
            }
            if (remoteOperation.mIndex <= mIndex) {
                return new Remove(mTargetUUID, mIndex + 1);
            }
            return this;
        }

        private EtherOperation xformOverRemove(Remove remoteOperation, boolean overrideRemote) {
            if (!remoteOperation.mTargetUUID.equals(mTargetUUID)) {
                return this;
            }
            if (remoteOperation.mIndex == mIndex) {
                return overrideRemote ? this : new NoOp();
            }
            if (remoteOperation.mIndex < mIndex) {
                return new Remove(mTargetUUID, mIndex - 1);
            }
            return this;
        }
    }

    public class ListReferenceValue extends AbstractValue {
        private ListReferenceValue() {}

        @Override
        public Class<?> getRequiredClass() {
            return ListNode.class;
        }

        @Override
        public ListNode asListReference() {
            return ListNode.this;
        }
    }
}
