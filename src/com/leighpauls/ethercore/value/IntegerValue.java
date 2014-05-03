package com.leighpauls.ethercore.value;

/**
 * Holds integer values
 */
public class IntegerValue extends AbstractValue {
    private final int mValue;

    public IntegerValue(int value) {
        mValue = value;
    }

    @Override
    public Class<?> getRequiredClass() {
        return Integer.class;
    }

    @Override
    public int asInt() {
        return mValue;
    }
}
