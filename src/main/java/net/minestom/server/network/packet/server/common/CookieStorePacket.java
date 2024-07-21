package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record CookieStorePacket(
        @NotNull String key, byte[] value
) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final int MAX_VALUE_LENGTH = 5120;

    public CookieStorePacket {
        Check.argCondition(value.length > MAX_VALUE_LENGTH, "Cookie value length too long: {0} > {1}", value.length, MAX_VALUE_LENGTH);
    }

    public CookieStorePacket(@NotNull NamespaceID key, byte[] value) {
        this(key.asString(), value);
    }

    public CookieStorePacket(@NotNull NetworkBuffer reader) {
        this(read(reader));
    }

    private CookieStorePacket(@NotNull CookieStorePacket other) {
        this(other.key, other.value);
    }

    private static @NotNull CookieStorePacket read(@NotNull NetworkBuffer reader) {
        String key = reader.read(NetworkBuffer.STRING);
        int valueLength = reader.read(NetworkBuffer.VAR_INT);
        Check.argCondition(valueLength > MAX_VALUE_LENGTH, "Cookie value length too long: {0} > {1}", valueLength, MAX_VALUE_LENGTH);
        byte[] value = reader.readBytes(valueLength);
        return new CookieStorePacket(key, value);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
       writer.write(NetworkBuffer.STRING, key);
       writer.write(NetworkBuffer.BYTE_ARRAY, value);
    }

}
