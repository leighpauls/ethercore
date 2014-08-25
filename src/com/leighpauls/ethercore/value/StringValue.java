package com.leighpauls.ethercore.value;

import com.google.common.base.Objects;
import com.leighpauls.ethercore.GraphDelegate;
import com.leighpauls.ethercore.util.SerializationUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    @Override
    public void serializeTypelessly(DataOutputStream output) throws IOException {
        SerializationUtils.serializeString(mValue, output);
    }

    public StringValue(DataInputStream inputStream) throws IOException {
        mValue = SerializationUtils.deserializeString(inputStream);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mValue);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StringValue && mValue == ((StringValue) obj).mValue;

    }
}
