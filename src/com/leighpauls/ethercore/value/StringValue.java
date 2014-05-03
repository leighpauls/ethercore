package com.leighpauls.ethercore.value;

/**
 * String implementation of an ether value
 */
public class StringValue extends AbstractValue {
    private final String mValue;

    public StringValue(String value) {
        mValue = value;
    }

    @Override
    public Class<?> getRequiredClass() {
        return String.class;
    }

    @Override
    public String asString() {
        return mValue;
    }
}
