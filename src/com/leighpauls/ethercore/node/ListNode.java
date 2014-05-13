package com.leighpauls.ethercore.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.leighpauls.ethercore.GraphDelegate;
import com.leighpauls.ethercore.OperationDelegate;
import com.leighpauls.ethercore.operation.EtherOperation;
import com.leighpauls.ethercore.operation.NoOp;
import com.leighpauls.ethercore.value.ListReferenceValue;
import com.leighpauls.ethercore.value.Value;
import com.leighpauls.ethercore.value.ValueData;

import java.util.ArrayList;
import java.util.UUID;

/**
 * List-based EtherCore Node
 */
public class ListNode extends AbstractNode {
    private final ArrayList<Value> mValues;
    private final ListReferenceValue mSelfReference;

    public ListNode(OperationDelegate operationDelegate, UUID uuid) {
        this(operationDelegate, uuid, Lists.<Value>newArrayList());
    }

    private ListNode(OperationDelegate operationDelegate, UUID uuid, ArrayList<Value> values) {
        super(operationDelegate, uuid);
        mValues = values;
        mSelfReference = new ListReferenceValue(this);
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
        getOperationDelegate().applyOperation(operation);
    }
    public void remove(int index) {
        Remove operation = new Remove(getUUID(), index);
        getOperationDelegate().applyOperation(operation);
    }

    @Override
    public NodeData serializeNode() {
        return new ListNodeData(this);
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
        public void apply(GraphDelegate delegate) {
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
        public void apply(GraphDelegate delegate) {
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

    public static class ListNodeData implements NodeData {
        private final UUID mUUID;
        private final ImmutableList<ValueData> mValues;

        public ListNodeData(ListNode node) {
            mUUID = node.getUUID();
            ImmutableList.Builder<ValueData> builder = ImmutableList.builder();
            for (Value value : node.mValues) {
                builder.add(value.serializeValue());
            }
            mValues = builder.build();
        }

        @Override
        public Node recreate(GraphDelegate graphDelegate) {
            ArrayList<Value> values = Lists.newArrayList();
            for (ValueData valueData : mValues) {
                values.add(valueData.recreate(graphDelegate));
            }
            return new ListNode(graphDelegate.getOperationDelegate(), mUUID, values);
        }
    }
}
