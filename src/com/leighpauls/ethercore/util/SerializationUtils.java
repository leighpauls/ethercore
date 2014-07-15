package com.leighpauls.ethercore.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Utilities for (de)serializing data using {@link java.io.DataOutputStream} and
 * {@link java.io.DataInputStream}
 */
public class SerializationUtils {
    public static void serializeUUID(UUID uuid, DataOutputStream outputStream) throws IOException {
        outputStream.writeLong(uuid.getMostSignificantBits());
        outputStream.writeLong(uuid.getLeastSignificantBits());
    }

    public static UUID deserializeUUID(DataInputStream inputStream) throws IOException {
        long msBits = inputStream.readLong();
        long lsBits = inputStream.readLong();
        return new UUID(msBits, lsBits);
    }

    public static String deserializeString(DataInputStream inputStream) throws IOException {
        int keyLength = inputStream.readInt();
        byte[] stringBuffer = new byte[keyLength];
        inputStream.readFully(stringBuffer);
        return new String(stringBuffer);
    }

    public static void serializeString(String value, DataOutputStream output) throws IOException {
        byte[] keyBytes = value.getBytes();
        output.writeInt(keyBytes.length);
        output.write(keyBytes);
    }
}
