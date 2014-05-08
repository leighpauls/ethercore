package com.leighpauls.ethercore.client;

import com.google.common.base.Objects;

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
}
