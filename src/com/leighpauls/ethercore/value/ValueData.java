package com.leighpauls.ethercore.value;

import com.leighpauls.ethercore.GraphDelegate;

/**
 * Serializable form of {@link com.leighpauls.ethercore.value.Value}
 */
public interface ValueData {
    Value recreate(GraphDelegate graphDelegate);
}
