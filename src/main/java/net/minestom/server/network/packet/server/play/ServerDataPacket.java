package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record ServerDataPacket(@Nullable Component motd, byte @Nullable [] iconBase64,
                               boolean enforcesSecureChat) implements ServerPacket.Play {
    public ServerDataPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(COMPONENT), reader.readOptional(BYTE_ARRAY),
                reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(COMPONENT, this.motd);
        writer.writeOptional(BYTE_ARRAY, this.iconBase64);
        writer.write(BOOLEAN, enforcesSecureChat);
    }

}
