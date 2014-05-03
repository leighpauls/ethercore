package com.leighpauls.ethercore.value;

import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.StructNode;

/**
 * Abstract implementation of values, dealing with casting errors
 */
public abstract class AbstractValue implements Value {

    public static class ValueTypeError extends RuntimeException {
        public ValueTypeError(Class<?> required, Class<?> requested) {
            super("Tried to read value of type " + required + " as " + requested);
        }
    }

    public abstract Class<?> getRequiredClass();

    @Override
    public int asInt() {
        throw new ValueTypeError(getRequiredClass(), Integer.class);
    }

    @Override
    public String asString() {
        throw new ValueTypeError(getRequiredClass(), String.class);
    }

    @Override
    public ListNode asListReference() {
        throw new ValueTypeError(getRequiredClass(), ListNode.class);
    }

    @Override
    public StructNode asStructReference() {
        throw new ValueTypeError(getRequiredClass(), StructNode.class);
    }
}
