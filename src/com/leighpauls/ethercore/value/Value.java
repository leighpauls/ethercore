package com.leighpauls.ethercore.value;

import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.StructNode;

/**
 * Top level interface of all values represented by nodes' elements
 */
public interface Value {
    int asInt();
    String asString();
    // TODO: the other types...

    ListNode asListReference();
    StructNode asStructReference();
}
