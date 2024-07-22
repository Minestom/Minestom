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
        public void write(@NotNull NetworkBuffer writer, @NotNull UpdateScorePacket value) {
            writer.write(STRING, value.entityName);
            writer.write(STRING, value.objectiveName);
            writer.write(VAR_INT, value.score);
            writer.writeOptional(COMPONENT, value.displayName);
            writer.writeOptional(value.numberFormat);
        }

        @Override
        public @NotNull UpdateScorePacket read(@NotNull NetworkBuffer reader) {
            return new UpdateScorePacket(reader.read(STRING), reader.read(STRING), reader.read(VAR_INT),
                    reader.readOptional(COMPONENT), reader.readOptional(Sidebar.NumberFormat::new));
        }
    };
}
