package com.leighpauls.ethercore.node;

import com.leighpauls.ethercore.GraphDelegate;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Holds the content of a node for serialization
 */
public interface NodeData {
    public Node recreate(GraphDelegate operationDelegate);

    /**
     * Serialize the object without regard to checking it's type. Don't call this method directly,
     * use {@link com.leighpauls.ethercore.node.NodeDataSerializer} instead to ensure type safety
     * @param output
     * @throws IOException
     */
    void serializeTypelessly(DataOutputStream output) throws IOException;
}
