package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

// public record ClientboundSetScorePacket(String owner, String objectiveName,
// int score, @Nullable Component display, @Nullable NumberFormat numberFormat) implements Packet<ClientGamePacketListener>
//{

public record UpdateScorePacket(
        @NotNull String entityName,
        @NotNull String objectiveName,
        int score,
        @Nullable Component displayName,
        @Nullable Sidebar.NumberFormat numberFormat
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<UpdateScorePacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, @NotNull UpdateScorePacket value) {
            buffer.write(STRING, value.entityName);
            buffer.write(STRING, value.objectiveName);
            buffer.write(VAR_INT, value.score);
            buffer.writeOptional(COMPONENT, value.displayName);
            buffer.writeOptional(value.numberFormat);
        }

        @Override
        public @NotNull UpdateScorePacket read(@NotNull NetworkBuffer buffer) {
            return new UpdateScorePacket(buffer.read(STRING), buffer.read(STRING), buffer.read(VAR_INT),
                    buffer.readOptional(COMPONENT), buffer.readOptional(Sidebar.NumberFormat::new));
        }
    };
}
