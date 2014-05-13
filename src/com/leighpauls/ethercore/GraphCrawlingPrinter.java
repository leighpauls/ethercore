package com.leighpauls.ethercore;

import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.Node;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.value.*;

/**
 * Prints a graph by crawling through it to some depth
 */
public class GraphCrawlingPrinter {

    private static void printValue(Object key, Value value, int maxDepth, int curDepth) {
        if (curDepth >= maxDepth) {
            return;
        }

        for (int i = 0; i < curDepth; ++i) {
            System.out.print("|");
        }
        System.out.print(" " + key + ": ");

        if (value instanceof IntegerValue) {
            System.out.println(value.asInt());
        } else if (value instanceof StringValue) {
            System.out.println(value.asString());
        } else if (value instanceof ListReferenceValue) {
            printNode(value.asListReference(), maxDepth, curDepth);
        }else if (value instanceof StructReferenceValue) {
            printNode(value.asStructReference(), maxDepth, curDepth);
        }
    }

    private static void printNode(Node node, int maxDepth, int curDepth) {
        if (curDepth >= maxDepth) {
            return;
        }
        if (node instanceof ListNode) {
            System.out.println("List: len(" + ((ListNode) node).size() + ") " + node.getUUID());
            for (int i = 0; i < ((ListNode) node).size(); ++i) {
                printValue(i, ((ListNode) node).get(i), maxDepth, curDepth + 1);
            }
        } else if (node instanceof StructNode) {
            System.out.println("Struct: " + node.getUUID());
            for (String key : ((StructNode) node).keySet()) {
                printValue(key, ((StructNode) node).get(key), maxDepth, curDepth + 1);
            }
        }
    }

    public static void printGraph(Node rootNode, int maxDepth) {
        printNode(rootNode, maxDepth, 0);
    }
}
