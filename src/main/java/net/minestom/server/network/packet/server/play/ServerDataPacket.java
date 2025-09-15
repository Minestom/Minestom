package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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

    public ServerDataPacket {
        iconBase64 = iconBase64 != null ? iconBase64.clone() : null;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ServerDataPacket(Component motd1, byte[] base64))) return false;

        return motd().equals(motd1) && Arrays.equals(iconBase64(), base64);
    }

    @Override
    public int hashCode() {
        int result = motd().hashCode();
        result = 31 * result + Arrays.hashCode(iconBase64());
        result = 31 * result + Boolean.hashCode(enforcesSecureChat());
        return result;
    }
}
