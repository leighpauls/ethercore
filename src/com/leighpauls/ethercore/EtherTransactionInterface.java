package com.leighpauls.ethercore;

import java.util.List;

/**
 * Interface to be implemented to build transactions
 */
public interface EtherTransactionInterface {
    /**
     * Implement this function to apply all changes for this transaction.
     * For the duration of this function call, all modifications to the graph are recorded, and they
     * are sent to the server as a transaction after this function returns.
     * @return a list of events to fire on each client after the recorded changes
     */
    List<EtherEvent> executeTransaction();
}
