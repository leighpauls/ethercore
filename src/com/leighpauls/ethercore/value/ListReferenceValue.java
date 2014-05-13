package com.leighpauls.ethercore.value;

import com.leighpauls.ethercore.GraphDelegate;
import com.leighpauls.ethercore.node.ListNode;

import java.util.UUID;

/**
 * Reference value pointing at a {@link com.leighpauls.ethercore.node.ListNode}
 */
public class ListReferenceValue extends AbstractValue {
    private ListNode mValue;
    private GraphDelegate mGraphDelegate;
    private final UUID mUUID;

    /**
     * Constructor to be used when the target value is already available
     * @param value The node this is pointing at
     */
    public ListReferenceValue(ListNode value) {
        mValue = value;
        mGraphDelegate = null;
        mUUID = value.getUUID();
    }

    /**
     * Constructor to use when the target node isn't already available (ie, the full graph hasn't
     * been initialized yet). This will make the reference act as a lazy memoizing lookup.
     * @param graphDelegate
     */
    private ListReferenceValue(GraphDelegate graphDelegate, UUID uuid) {
        mValue = null;
        mGraphDelegate = graphDelegate;
        mUUID = uuid;
    }

    @Override
    public Class<?> getRequiredClass() {
        return ListNode.class;
    }

    @Override
    public ListNode asListReference() {
        if (mValue == null) {
            mValue = (ListNode) mGraphDelegate.getNode(mUUID);
        }
        return mValue;
    }

    @Override
    public ValueData serializeValue() {
        return null;
    }

    private static class ListReferenceValueData implements ValueData {
        private final UUID mUUID;
        private ListReferenceValueData(UUID uuid) {
            mUUID = uuid;
        }

        @Override
        public Value recreate(GraphDelegate graphDelegate) {
            return new ListReferenceValue(graphDelegate, mUUID);
        }
    }
}
