package com.leighpauls.ethercore.except;

/**
 * thrown if there is no implementation of the deserialization constructor for a primitive type.
 */
public class DeserializationConstructorNotImplemented extends EtherRuntimeException {
    public DeserializationConstructorNotImplemented(Class<?> type, Exception e) {
        super(
                "Couldn't deserialize an instance of "
                        + type + "because it's missing a constructor",
                e);
    }
}
