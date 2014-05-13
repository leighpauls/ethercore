package com.leighpauls.ethercore.server;

import com.google.common.collect.ImmutableMap;
import com.leighpauls.ethercore.client.ClientClock;

import java.util.Map;
import java.util.UUID;

/**
 * Vector clock that has an entry for each client's state
 */
public class ServerClock {
    private final ImmutableMap<UUID, Integer> mClocks;
    private final int mTotalState;

    public ServerClock(ImmutableMap<UUID, Integer> clientClocks, int totalState) {
        mClocks = clientClocks;
        mTotalState = totalState;
    }

    /**
     * @param advancingClient
     * @return The clock state that comes after a transaction from advancingClient on this state
     */
    public ServerClock nextState(UUID advancingClient) {
        ImmutableMap.Builder<UUID, Integer> builder = ImmutableMap.<UUID, Integer>builder();

        // copy each existing value, updating the advacing client
        boolean alreadyHaveClient = false;
        for (Map.Entry<UUID, Integer> client : mClocks.entrySet()) {
            UUID key = client.getKey();
            if (key.equals(advancingClient)) {
                builder.put(advancingClient, client.getValue() + 1);
                alreadyHaveClient = true;
                continue;
            }
            builder.put(key, client.getValue());
        }
        if (!alreadyHaveClient) {
            builder.put(advancingClient, 1);
        }

        return new ServerClock(builder.build(), mTotalState + 1);
    }

    /**
     * @param clientUUID
     * @return This clock the given client's clock space
     */
    public ClientClock forClient(UUID clientUUID) {
        int localState;
        if (!mClocks.containsKey(clientUUID)) {
            localState = 0;
        } else {
            localState = mClocks.get(clientUUID);
        }
        return new ClientClock(localState, mTotalState - localState);
    }
}
