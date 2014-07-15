package com.leighpauls.ethercore.operation;

import com.leighpauls.ethercore.GraphDelegate;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * All operations (things that can mutate the tree) implement this
 */
public interface EtherOperation {
    /**
     * Apply the operation
     * @param delegate delegate of the EtherClient to apply the operation to
     */
    void apply(GraphDelegate delegate);

    EtherOperation transformOver(EtherOperation remoteOperation, boolean overrideRemote);

    /**
     * Write the operation without any type information. Do not call this directly, only
     * {@link com.leighpauls.ethercore.operation.EtherOperationSerializer} should every use this to
     * ensure type safety over marshalling
     * @param outputStream
     */
    void serializeTypelessly(DataOutputStream outputStream) throws IOException;


}
