package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.common.CookieStorePacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ClientCookieResponsePacket(
        @NotNull String key,
        byte @Nullable [] value
) implements ClientPacket {

    public ClientCookieResponsePacket {
        Check.argCondition(value != null && value.length > CookieStorePacket.MAX_VALUE_LENGTH,
                "Value is too long: {0} > {1}", value != null ? value.length : 0, CookieStorePacket.MAX_VALUE_LENGTH);
    }

    public ClientCookieResponsePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.STRING), reader.readOptional(buffer -> {
            int valueLength = buffer.read(NetworkBuffer.VAR_INT);
            Check.argCondition(valueLength > CookieStorePacket.MAX_VALUE_LENGTH,
                    "Value is too long: {0} > {1}", valueLength, CookieStorePacket.MAX_VALUE_LENGTH);
            return buffer.readBytes(valueLength);
        }));
    }

    @Override
    public boolean processImmediately() {
        return true;
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.STRING, key);
        writer.writeOptional(NetworkBuffer.BYTE_ARRAY, value);
    }

}
