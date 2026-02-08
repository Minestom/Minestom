package net.minestom.server.network.template;

import net.minestom.server.network.NetworkBuffer;

import java.lang.constant.ClassDesc;

/// Implementing class must override the writeT/readT, even if the backing buffer doesnt use it.
public interface PrimitiveType<T> extends NetworkBuffer.Type<T> {
    /// The primitive type the class represents.
    Class<T> primitiveClass();

    default String writeMethodName() {
        String className = primitiveClass().getSimpleName();
        return "write" + capitalize(className);
    }

    default String readMethodName() {
        String className = primitiveClass().getSimpleName();
        return "read" + capitalize(className);
    }

    default ClassDesc classDesc() {
        return primitiveClass().describeConstable().orElseThrow();
    }

    interface Unsigned {}

    default void writeBoolean(NetworkBuffer buffer, boolean value) {
        throw new UnsupportedOperationException();
    }

    default void writeByte(NetworkBuffer buffer, byte value) {
        throw new UnsupportedOperationException();
    }

    default void writeShort(NetworkBuffer buffer, short value) {
        throw new UnsupportedOperationException();
    }

    default void writeChar(NetworkBuffer buffer, char value) {
        throw new UnsupportedOperationException();
    }

    default void writeInt(NetworkBuffer buffer, int value) {
        throw new UnsupportedOperationException();
    }

    default void writeLong(NetworkBuffer buffer, long value) {
        throw new UnsupportedOperationException();
    }

    default void writeFloat(NetworkBuffer buffer, float value) {
        throw new UnsupportedOperationException();
    }

    default void writeDouble(NetworkBuffer buffer, double value) {
        throw new UnsupportedOperationException();
    }

    default boolean readBoolean(NetworkBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    default byte readByte(NetworkBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    default short readShort(NetworkBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    default char readChar(NetworkBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    default int readInt(NetworkBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    default long readLong(NetworkBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    default float readFloat(NetworkBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    default double readDouble(NetworkBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
