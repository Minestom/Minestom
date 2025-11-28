package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record ServerDataPacket(Component motd, byte @Nullable [] iconBase64) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ServerDataPacket> SERIALIZER = NetworkBufferTemplate.template(
            COMPONENT, ServerDataPacket::motd,
            BYTE_ARRAY.optional(), ServerDataPacket::iconBase64,
            ServerDataPacket::new);

    /**
     * No longer exists
     * @deprecated Use {@link ServerDataPacket(Component, byte[])}
     * @param motd motd
     * @param iconBase64 icon
     * @param forRemoval enforcesSecureChat
     */
    @Deprecated(forRemoval = true)
    public ServerDataPacket(Component motd, @Nullable byte[] iconBase64, boolean forRemoval) {
        this(motd, iconBase64);
    }
    /**
     * No longer exists
     * @deprecated Use {@link ServerDataPacket(Component, byte[])}
     */
    @Deprecated(forRemoval = true)
    boolean enforcesSecureChat() {
        return false;
    }
}
