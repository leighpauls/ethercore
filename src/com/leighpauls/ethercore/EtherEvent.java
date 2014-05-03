package com.leighpauls.ethercore;

/**
 * Events triggered and monitored by the user-space to alert the local and remote agents about
 * changes made in an associated transaction
 */
public class EtherEvent {
    private final String mName;

    public EtherEvent(String eventName) {
        mName = eventName;
    }

    public String getName() {
        return mName;
    }
}
