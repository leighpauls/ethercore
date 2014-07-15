package com.leighpauls.ethercore.node;

import com.google.common.collect.ImmutableBiMap;
import com.leighpauls.ethercore.except.DeserializationConstructorNotImplemented;
import com.leighpauls.ethercore.value.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Handles the type-conscious (de)serialization of Values
 */
public class ValueDataSerializer {
    private static enum ValueTypes {
        INTEGER(1),
        STRING(2),
        LIST_REFERENCE(3),
        STRUCT_REFERENCE(4);

        public final int value;
        private ValueTypes(int hardValue) {
            value = hardValue;
        }
    }

    private static final ImmutableBiMap<Integer, Class<? extends ValueData>> typeEncodings =
            ImmutableBiMap.<Integer, Class<? extends ValueData>>builder()
                    .put(ValueTypes.INTEGER.value, IntegerValue.class)
                    .put(ValueTypes.STRING.value, StringValue.class)
                    .put(
                            ValueTypes.LIST_REFERENCE.value,
                            ListReferenceValue.ListReferenceValueData.class)
                    .put(
                            ValueTypes.STRUCT_REFERENCE.value,
                            StructReferenceValue.StructReferenceValueData.class)
                    .build();



    public static ValueData deserialize(DataInputStream inputStream) throws IOException {
        Class<? extends ValueData> valueType = typeEncodings.get(inputStream.readInt());
        try {
            return valueType.getConstructor(DataInputStream.class).newInstance(inputStream);
        } catch (Exception e) {
            throw new DeserializationConstructorNotImplemented(valueType, e);
        }
    }

    public static void serialize(ValueData value, DataOutputStream output) throws IOException {
        // prepend the type
        output.writeInt(typeEncodings.inverse().get(value.getClass()));
        // write the value
        value.serializeTypelessly(output);
    }
}
