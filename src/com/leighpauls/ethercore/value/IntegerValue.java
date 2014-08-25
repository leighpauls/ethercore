package com.leighpauls.ethercore.value;

import com.google.common.base.Objects;
import com.leighpauls.ethercore.GraphDelegate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Holds integer values
 */
public class IntegerValue extends AbstractValue implements ValueData {
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

    @Override
    public ValueData serializeValue() {
        return this;
    }

    @Override
    public Value recreate(GraphDelegate graphDelegate) {
        return this;
    }

    @Override
    public void serializeTypelessly(DataOutputStream output) throws IOException {
        output.writeInt(mValue);
    }

    public IntegerValue(DataInputStream inputStream) throws IOException {
        mValue = inputStream.readInt();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mValue);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IntegerValue && mValue == ((IntegerValue) other).mValue;
    }
}
