package com.leighpauls.ethercore.client;

import com.google.common.base.Objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Vector-clock from a clients perspective, understanding only local and non-local states (rather
 * than every local state)
 */
public class ClientClock {
    private final int mLocalState;
    private final int mRemoteState;

    public ClientClock(int localState, int remoteState) {
        mLocalState = localState;
        mRemoteState = remoteState;
    }

    public int getLocalState() {
        return mLocalState;
    }

    public int getRemoteState() {
        return mRemoteState;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mLocalState, mRemoteState);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || other.getClass() != ClientClock.class) {
            return false;
        }
        ClientClock that = (ClientClock) other;
        return mLocalState == that.mLocalState
                && mRemoteState == that.mRemoteState;
    }

    public ClientClock nextLocalState() {
        return new ClientClock(mLocalState + 1, mRemoteState);
    }

    public ClientClock nextRemoteState() {
        return new ClientClock(mLocalState, mRemoteState + 1);
    }

    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(mLocalState);
        outputStream.writeInt(mRemoteState);
    }

    public ClientClock(DataInputStream inputStream) throws IOException {
        mLocalState = inputStream.readInt();
        mRemoteState = inputStream.readInt();
    }

}
