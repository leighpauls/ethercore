package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.EtherClient;
import com.leighpauls.ethercore.operation.EtherOperation;
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

    public static class Insert implements EtherOperation<ListNode> {
        private final UUID mUUID;
        private final int mIndex;
        private final Value mValue;

        public Insert(UUID uuid, int index, Value value) {
            mUUID = uuid;
            mIndex = index;
            mValue = value;
        }

        @Override
        public ListNode apply(EtherClient.OperationDelegate delegate) {
            ListNode target = (ListNode) delegate.getNode(mUUID);
            target.mValues.add(mIndex, mValue);
            return target;
        }
    }

    private static class Remove implements EtherOperation<ListNode> {
        private final UUID mUUID;
        private final int mIndex;

        private Remove(UUID uuid, int index) {
            mUUID = uuid;
            mIndex = index;
        }

        @Override
        public ListNode apply(EtherClient.OperationDelegate delegate) {
            ListNode target = (ListNode) delegate.getNode(mUUID);
            target.mValues.remove(mIndex);
            return target;
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
