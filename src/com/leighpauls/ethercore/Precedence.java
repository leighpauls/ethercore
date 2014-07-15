package com.leighpauls.ethercore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents the mPrecedence of a particular agent
 */
public class Precedence {
    private final int mPrecedence;

    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(mPrecedence);
    }

    public Precedence(int precedence) {
        mPrecedence = precedence;
    }

    public Precedence(DataInputStream inputStream) throws IOException {
        mPrecedence = inputStream.readInt();
    }

    public boolean overrides(Precedence other) {
        return mPrecedence < other.mPrecedence;
    }


}
