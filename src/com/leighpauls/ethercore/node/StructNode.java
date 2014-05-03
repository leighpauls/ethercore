package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.EtherClient;
import com.leighpauls.ethercore.value.AbstractValue;
import com.leighpauls.ethercore.value.Value;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Struct-based EtherCore Node (named field object)
 */
public class StructNode extends AbstractNode {
    private final HashMap<String, Value> mValues;
    private final StructReferenceValue mSelfReference;

    public StructNode(EtherClient.EtherClientDelegate etherClientDelegate, UUID uuid) {
        super(etherClientDelegate, uuid);
        mValues = new HashMap<String, Value>();
        mSelfReference = new StructReferenceValue();
    }

    public Value get(String key) {
        return mValues.get(key);
    }

    public Set<String> keySet() {
        return mValues.keySet();
    }

    public StructReferenceValue getReference() {
        return mSelfReference;
    }

    // mutation operations, protected by transaction restrictions
    public void put(String key, Value value) {
        verifyNodeIsMutable();

        // TODO: build the operation and put it in the transaction
        mValues.put(key, value);
    }
    public void remove(String key) {
        verifyNodeIsMutable();

        // TODO: build the operation
        mValues.remove(key);
    }

    public class StructReferenceValue extends AbstractValue {
        private StructReferenceValue() {}

        @Override
        public Class<?> getRequiredClass() {
            return StructNode.class;
        }

        @Override
        public StructNode asStructReference() {
            return StructNode.this;
        }
    }
}
