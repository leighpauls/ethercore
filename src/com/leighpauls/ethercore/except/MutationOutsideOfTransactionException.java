package com.leighpauls.ethercore.except;

/**
 * Thrown if a mutable operation is applied to the ether graph while not inside of a transaction
 */
public class MutationOutsideOfTransactionException extends EtherRuntimeException {
    public MutationOutsideOfTransactionException() {
        super("Tried to modify ether graph outside of a transaction");
    }
}
