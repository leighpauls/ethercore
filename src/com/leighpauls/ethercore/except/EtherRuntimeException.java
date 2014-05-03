package com.leighpauls.ethercore.except;

/**
 * Debug catch-all shell runtime exception
 */
public class EtherRuntimeException extends RuntimeException {
    public EtherRuntimeException(String s) {
        super(s);
    }
}
