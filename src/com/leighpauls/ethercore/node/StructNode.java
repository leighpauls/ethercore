package com.leighpauls.ethercore.node;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.leighpauls.ethercore.GraphDelegate;
import com.leighpauls.ethercore.OperationDelegate;
import com.leighpauls.ethercore.operation.EtherOperation;
import com.leighpauls.ethercore.operation.NoOp;
import com.leighpauls.ethercore.util.SerializationUtils;
import com.leighpauls.ethercore.value.StructReferenceValue;
import com.leighpauls.ethercore.value.Value;
import com.leighpauls.ethercore.value.ValueData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Struct-based EtherCore Node (named field object)
 */
public class StructNode extends AbstractNode {
    private final HashMap<String, Value> mValues;
    private final StructReferenceValue mSelfReference;

    public StructNode(OperationDelegate operationDelegate, UUID uuid) {
        this(operationDelegate, uuid, Maps.<String, Value>newHashMap());
    }

    private StructNode(
            OperationDelegate operationDelegate,
            UUID uuid,
            HashMap<String, Value> values) {
        super(operationDelegate, uuid);
        mValues = values;
        mSelfReference = new StructReferenceValue(this);
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
        Put operation = new Put(getUUID(), key, value.serializeValue());
        getOperationDelegate().applyOperation(operation);
    }
    public void remove(String key) {
        Clear operation = new Clear(getUUID(), key);
        getOperationDelegate().applyOperation(operation);
    }

    @Override
    public NodeData serializeNode() {
        return new StructNodeData(this);
    }

    /**
     * Operation to put a value into a {@link StructNode}
     */
    public static class Put implements EtherOperation {
        private final UUID mTargetUUID;
        private final String mKey;
        private final ValueData mValueData;

        public Put(UUID uuid, String key, ValueData valueData) {
            mTargetUUID = uuid;
            mKey = key;
            mValueData = valueData;
        }

        @Override
        public void apply(GraphDelegate delegate) {
            StructNode target = (StructNode) delegate.getNode(mTargetUUID);
            target.mValues.put(mKey, mValueData.recreate(delegate));
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

        public Put(DataInputStream inputStream) throws IOException {
            mTargetUUID = SerializationUtils.deserializeUUID(inputStream);
            mKey = SerializationUtils.deserializeString(inputStream);
            mValueData = ValueDataSerializer.deserialize(inputStream);
        }

        @Override
        public void serializeTypelessly(DataOutputStream outputStream) throws IOException {
            SerializationUtils.serializeUUID(mTargetUUID, outputStream);
            SerializationUtils.serializeString(mKey, outputStream);
            ValueDataSerializer.serialize(mValueData, outputStream);
        }
    }

    public static class Clear implements EtherOperation {
        private final UUID mTargetUUID;
        private final String mKey;

        public Clear(UUID uuid, String key) {
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
            } else if (remoteOperation instanceof Clear) {
                return xformOverRemove((Clear) remoteOperation, overrideRemote);
            }
            // no conflict possible
            return this;
        }

        public Clear(DataInputStream inputStream) throws IOException {
            mTargetUUID = SerializationUtils.deserializeUUID(inputStream);
            mKey = SerializationUtils.deserializeString(inputStream);
        }

        @Override
        public void serializeTypelessly(DataOutputStream outputStream) throws IOException {
            SerializationUtils.serializeUUID(mTargetUUID, outputStream);
            SerializationUtils.serializeString(mKey, outputStream);
        }

        private EtherOperation xformOverRemove(Clear remoteOperation, boolean overrideRemote) {
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

    public static class StructNodeData implements NodeData {
        private final UUID mUUID;
        private final ImmutableMap<String, ValueData> mValues;

        public StructNodeData(StructNode structNode) {
            mUUID = structNode.getUUID();

            ImmutableMap.Builder<String, ValueData> builder = ImmutableMap.builder();
            for (Map.Entry<String, Value> entry : structNode.mValues.entrySet()) {
                builder.put(entry.getKey(), entry.getValue().serializeValue());
            }
            mValues = builder.build();
        }

        public StructNodeData(DataInputStream inputStream) throws IOException {
            mUUID = SerializationUtils.deserializeUUID(inputStream);

            int numValues = inputStream.readInt();
            ImmutableMap.Builder<String, ValueData> builder = ImmutableMap.builder();
            for (int i = 0; i < numValues; ++i) {
                builder.put(
                        SerializationUtils.deserializeString(inputStream),
                        ValueDataSerializer.deserialize(inputStream));
            }
            mValues = builder.build();
        }

        @Override
        public void serializeTypelessly(DataOutputStream output) throws IOException {
            SerializationUtils.serializeUUID(mUUID, output);

            output.writeInt(mValues.size());
            for (Map.Entry<String, ValueData> valueDataEntry : mValues.entrySet()) {
                SerializationUtils.serializeString(valueDataEntry.getKey(), output);
                ValueDataSerializer.serialize(valueDataEntry.getValue(), output);
            }
        }

        @Override
        public Node recreate(GraphDelegate graphDelegate) {
            HashMap<String, Value> values = Maps.newHashMap();
            for (Map.Entry<String, ValueData> valueDataEntry : mValues.entrySet()) {
                values.put(
                        valueDataEntry.getKey(),
                        valueDataEntry.getValue().recreate(graphDelegate));
            }
            return new StructNode(graphDelegate.getOperationDelegate(), mUUID, values);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(mUUID, mValues);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof StructNodeData
                    && Objects.equal(mUUID, ((StructNodeData) other).mUUID)
                    && Objects.equal(mValues, ((StructNodeData) other).mValues);
        }
    }
}
