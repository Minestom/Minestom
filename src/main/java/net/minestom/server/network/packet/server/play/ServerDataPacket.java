package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record ServerDataPacket(@Nullable Component motd, @Nullable String iconBase64,
                               boolean enforcesSecureChat) implements ServerPacket {
    public ServerDataPacket(@NotNull NetworkBuffer reader) {
        this(reader.readOptional(COMPONENT), reader.readOptional(STRING),
                reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeOptional(COMPONENT, this.motd);
        writer.writeOptional(STRING, this.iconBase64);
        writer.write(BOOLEAN, enforcesSecureChat);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SERVER_DATA;
    }
}
