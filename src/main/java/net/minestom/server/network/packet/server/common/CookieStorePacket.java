package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record CookieStorePacket(
        @NotNull String key, byte[] value
) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final int MAX_VALUE_LENGTH = 5120;

    public static final NetworkBuffer.Type<CookieStorePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, CookieStorePacket::key,
            NetworkBuffer.BYTE_ARRAY, CookieStorePacket::value,
            CookieStorePacket::new);

    public CookieStorePacket {
        Check.argCondition(value.length > MAX_VALUE_LENGTH, "Cookie value length too long: {0} > {1}", value.length, MAX_VALUE_LENGTH);
    }

    public CookieStorePacket(@NotNull NamespaceID key, byte[] value) {
        this(key.asString(), value);
    }
}
