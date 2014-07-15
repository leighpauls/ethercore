package com.leighpauls.ethercore.except;

/**
 * Debug catch-all shell runtime exception
 */
public class EtherRuntimeException extends RuntimeException {
    public EtherRuntimeException(String s) {
        super(s);
    }
    public EtherRuntimeException(Throwable throwable) {
        super(throwable);
    }

    public EtherRuntimeException(String s, Throwable e) {
        super(s, e);
    }
}
