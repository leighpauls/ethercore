package com.leighpauls.ethercore.value;

import com.leighpauls.ethercore.GraphDelegate;

/**
 * String implementation of an ether value
 */
public class StringValue extends AbstractValue implements ValueData {
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

    @Override
    public ValueData serializeValue() {
        return this;
    }

    @Override
    public Value recreate(GraphDelegate graphDelegate) {
        return this;
    }
}
