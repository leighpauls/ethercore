package com.leighpauls.ethercore.value;

import com.leighpauls.ethercore.GraphDelegate;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.util.SerializationUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Reference to a {@link com.leighpauls.ethercore.node.StructNode}
 */
public class StructReferenceValue extends AbstractValue {
    private StructNode mValue;
    private GraphDelegate mGraphDelegate;
    private final UUID mUUID;

    /**
     * Constructor used when the target value is already available
     * @param value The node this is pointing at
     */
    public StructReferenceValue(StructNode value) {
        mValue = value;
        mGraphDelegate = null;
        mUUID = value.getUUID();
    }

    /**
     * Constructor to use when the target node isn't already available (ie, the full graph hasn't
     * been initialized yet). This will make the reference act as a lazy memoizing lookup.
     * @param graphDelegate
     */
    private StructReferenceValue(GraphDelegate graphDelegate, UUID uuid) {
        mValue = null;
        mGraphDelegate = graphDelegate;
        mUUID = uuid;
    }

    @Override
    public Class<?> getRequiredClass() {
        return StructNode.class;
    }

    @Override
    public ValueData serializeValue() {
        return new StructReferenceValueData(mUUID);
    }

    public static class StructReferenceValueData implements ValueData {
        private final UUID mUUID;
        private StructReferenceValueData(UUID uuid) {
            mUUID = uuid;
        }

        @Override
        public Value recreate(GraphDelegate graphDelegate) {
            return new StructReferenceValue(graphDelegate, mUUID);
        }

        @Override
        public void serializeTypelessly(DataOutputStream output) throws IOException {
            SerializationUtils.serializeUUID(mUUID, output);
        }

        public StructReferenceValueData(DataInputStream inputStream) throws IOException {
            mUUID = SerializationUtils.deserializeUUID(inputStream);
        }
    }

    @Override
    public StructNode asStructReference() {
        if (mValue == null) {
            mValue = (StructNode) mGraphDelegate.getNode(mUUID);
        }
        return mValue;
    }
}
