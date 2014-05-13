package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.GraphDelegate;
import com.leighpauls.ethercore.OperationDelegate;
import com.leighpauls.ethercore.operation.EtherOperation;
import com.leighpauls.ethercore.operation.NoOp;
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

    public StructNode(OperationDelegate operationDelegate, UUID uuid) {
        super(operationDelegate, uuid);
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
        getOperationDelegate().applyOperation(operation);
    }
    public void remove(String key) {
        Remove operation = new Remove(getUUID(), key);
        getOperationDelegate().applyOperation(operation);
    }

    public static class Put implements EtherOperation {
        private final UUID mTargetUUID;
        private final String mKey;
        private final Value mValue;

        public Put(UUID uuid, String key, Value value) {
            mTargetUUID = uuid;
            mKey = key;
            mValue = value;
        }

        @Override
        public void apply(GraphDelegate delegate) {
            StructNode target = (StructNode) delegate.getNode(mTargetUUID);
            target.mValues.put(mKey, mValue);
        }

        @Override
        public EtherOperation transformOver(
                EtherOperation remoteOperation,
                boolean overrideRemote) {
            // only a Put can conflict with a Put
            if (!(remoteOperation instanceof Put)) {
                return this;
            }
            // is it non-conflicting or overriding
            Put other = (Put)remoteOperation;
            if ((!other.mTargetUUID.equals(mTargetUUID))
                    || (!other.mKey.equals(mKey))
                    || overrideRemote) {
                return this;
            }
            // I was overridden
            return new NoOp();
        }
    }

    public static class Remove implements EtherOperation {
        private final UUID mTargetUUID;
        private final String mKey;

        public Remove(UUID uuid, String key) {
            mTargetUUID = uuid;
            mKey = key;
        }

        @Override
        public void apply(GraphDelegate delegate) {
            StructNode target = (StructNode) delegate.getNode(mTargetUUID);
            target.mValues.remove(mKey);
        }

        @Override
        public EtherOperation transformOver(
                EtherOperation remoteOperation,
                boolean overrideRemote) {
            if (remoteOperation instanceof Put) {
                return xformOverPut((Put) remoteOperation);
            } else if (remoteOperation instanceof Remove) {
                return xformOverRemove((Remove) remoteOperation, overrideRemote);
            }
            // no conflict possible
            return this;
        }

        private EtherOperation xformOverRemove(Remove remoteOperation, boolean overrideRemote) {
            if ((!remoteOperation.mTargetUUID.equals(mTargetUUID))
                    || (!remoteOperation.mKey.equals(mKey))
                    || (overrideRemote)) {
                return this;
            }
            // I'm being overridden
            return new NoOp();
        }

        private EtherOperation xformOverPut(Put remoteOperation) {
            if ((!remoteOperation.mTargetUUID.equals(mTargetUUID))
                    || (!remoteOperation.mKey.equals(mKey))) {
                return this;
            }
            // the remove will be done implicitly by the put
            return new NoOp();
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
