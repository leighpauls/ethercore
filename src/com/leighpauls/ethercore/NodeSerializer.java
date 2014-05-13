package com.leighpauls.ethercore;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.leighpauls.ethercore.node.Node;
import com.leighpauls.ethercore.node.NodeData;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

/**
 * Serialize and deserialize nodes
 */
public class NodeSerializer {
    public static ImmutableSet<NodeData> serializeNodes(Collection<Node> nodes) {
        ImmutableSet.Builder<NodeData> builder = ImmutableSet.builder();
        for (Node node : nodes) {
            builder.add(node.serializeNode());
        }
        return builder.build();
    }

    public static HashMap<UUID, Node> recreateNodes(
            ImmutableSet<NodeData> nodes,
            GraphDelegate graphDelegate) {
        HashMap<UUID, Node> result = Maps.newHashMap();
        for (NodeData nodeData : nodes) {
            Node node = nodeData.recreate(graphDelegate);
            result.put(node.getUUID(), node);
        }
        return result;
    }
}
