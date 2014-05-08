package com.leighpauls.ethercore;

/**
 * Represents the mPrecedence of a particular agent
 */
public class Precedence {
    private final int mPrecedence;

    public Precedence(int precedence) {
        mPrecedence = precedence;
    }

    public boolean overrides(Precedence other) {
        return mPrecedence < other.mPrecedence;
    }
}
