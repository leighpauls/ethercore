package com.leighpauls.ethercore;

import com.leighpauls.ethercore.operation.EtherOperation;

/**
 * Interface which exposes methods needed for applying and recording operations
 */
public interface OperationDelegate {
    void applyOperation(EtherOperation operation);
}
