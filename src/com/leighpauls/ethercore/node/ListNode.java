package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.EtherClient;
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

    public ListNode(EtherClient.EtherClientDelegate etherClientDelegate, UUID uuid) {
        super(etherClientDelegate, uuid);
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
        verifyNodeIsMutable();

        // TODO: build the operation and append it to the transaction
        mValues.add(index, value);
    }
    public void remove(int index) {
        verifyNodeIsMutable();

        // TODO: build the operation and append it to the transaction
        mValues.remove(index);
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
