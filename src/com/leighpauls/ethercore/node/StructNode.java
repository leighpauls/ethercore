package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.EtherClient;
import com.leighpauls.ethercore.operation.EtherOperation;
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

    public StructNode(EtherClient.GraphDelegate graphDelegate, UUID uuid) {
        super(graphDelegate, uuid);
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
        Put operation = new Put(getUUID(), key, value);
        getGraphDelegate().applyOperation(operation);
    }
    public void remove(String key) {
        Remove operation = new Remove(getUUID(), key);
        getGraphDelegate().applyOperation(operation);
    }

    public static class Put implements EtherOperation<StructNode> {
        private final UUID mUUID;
        private final String mKey;
        private final Value mValue;

        public Put(UUID uuid, String key, Value value) {
            mUUID = uuid;
            mKey = key;
            mValue = value;
        }

        @Override
        public StructNode apply(EtherClient.OperationDelegate delegate) {
            StructNode target = (StructNode) delegate.getNode(mUUID);
            target.mValues.put(mKey, mValue);
            return target;
        }
    }

    public static class Remove implements EtherOperation<StructNode> {
        private final UUID mUUID;
        private final String mKey;

        public Remove(UUID uuid, String key) {
            mUUID = uuid;
            mKey = key;
        }

        @Override
        public StructNode apply(EtherClient.OperationDelegate delegate) {
            StructNode target = (StructNode) delegate.getNode(mUUID);
            target.mValues.remove(mKey);
            return target;
        }
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
