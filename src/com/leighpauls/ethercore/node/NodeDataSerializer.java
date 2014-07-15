package com.leighpauls.ethercore.node;

import com.google.common.collect.ImmutableBiMap;
import com.leighpauls.ethercore.except.DeserializationConstructorNotImplemented;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Handles type-conscious (de)serialization of nodes
 */
public class NodeDataSerializer {
    private static enum NodeTypes {
        ListNodeType(1), StructNodeType(2);

        private final int value;
        private NodeTypes(int hardValue) {
            value = hardValue;
        }
    }

    private static final ImmutableBiMap<Integer, Class<? extends NodeData>> typeEncodings =
            ImmutableBiMap.<Integer, Class<? extends NodeData>>builder()
                    .put(NodeTypes.ListNodeType.value, ListNode.ListNodeData.class)
                    .put(NodeTypes.StructNodeType.value, StructNode.StructNodeData.class)
                    .build();

    public static void serialize(NodeData node, DataOutputStream output) throws IOException {
        // prepend the type
        output.writeInt(typeEncodings.inverse().get(node.getClass()));
        node.serializeTypelessly(output);
    }

    public static NodeData deserialize(DataInputStream input) throws IOException {
        Class<? extends NodeData> nodeType = typeEncodings.get(input.readInt());
        try {
            return nodeType.getConstructor(DataInputStream.class).newInstance(input);
        } catch (Exception e) {
            throw new DeserializationConstructorNotImplemented(nodeType, e);
        }
    }
}
