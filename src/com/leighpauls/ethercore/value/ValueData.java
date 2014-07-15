package com.leighpauls.ethercore.value;

import com.leighpauls.ethercore.GraphDelegate;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Serializable form of {@link com.leighpauls.ethercore.value.Value}
 */
public interface ValueData {
    Value recreate(GraphDelegate graphDelegate);

    /**
     * Serializes the content of this value without any type information. Do not call this method
     * directly. Use {@link com.leighpauls.ethercore.node.ValueDataSerializer} instead to preserve
     * type-safety.
     * @param output
     * @throws IOException
     */
    void serializeTypelessly(DataOutputStream output) throws IOException;
}
