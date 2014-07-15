package com.leighpauls.ethercore.operation;

import com.google.common.collect.ImmutableBiMap;
import com.leighpauls.ethercore.except.DeserializationConstructorNotImplemented;
import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.StructNode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * (De)marshalls serialized {@link com.leighpauls.ethercore.operation.EtherOperation}s with type
 * information.
 */
public class EtherOperationSerializer {

    private static enum OperationType {
        NoOpType(1),

        CreateListType(2),
        ListInsertType(3),
        ListRemoveType(4),

        CreateStructType(5),
        StructPutType(6),
        StructRemoveType(7);

        public final int value;

        private OperationType(int hardValue) {
            value = hardValue;
        }
    }

    private static final ImmutableBiMap<Integer, Class<? extends EtherOperation>> typeEncodings =
            ImmutableBiMap.<Integer, Class<? extends EtherOperation>>builder()
                    .put(OperationType.NoOpType.value, NoOp.class)
                    .put(OperationType.CreateListType.value, CreateList.class)
                    .put(OperationType.ListInsertType.value, ListNode.Insert.class)
                    .put(OperationType.ListRemoveType.value, ListNode.Remove.class)
                    .put(OperationType.CreateStructType.value, CreateStruct.class)
                    .put(OperationType.StructPutType.value, StructNode.Put.class)
                    .put(OperationType.StructRemoveType.value, StructNode.Clear.class)
                    .build();

    public static void serialize(DataOutputStream outputStream, EtherOperation operation)
            throws IOException {
        // write the operation type
        outputStream.writeInt(typeEncodings.inverse().get(operation.getClass()));
        operation.serializeTypelessly(outputStream);
    }

    public static EtherOperation deserialize(DataInputStream inputStream) throws IOException {
        Class<? extends EtherOperation> operationClass = typeEncodings.get(inputStream.readInt());
        try {
            return operationClass.getConstructor(DataInputStream.class).newInstance(inputStream);
        } catch (Exception e) {
            throw new DeserializationConstructorNotImplemented(operationClass, e);
        }
    }

}
