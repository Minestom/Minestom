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
    public UpdateScorePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(STRING), reader.read(VAR_INT),
                reader.readOptional(COMPONENT), reader.readOptional(Sidebar.NumberFormat::new));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, entityName);
        writer.write(STRING, objectiveName);
        writer.write(VAR_INT, score);
        writer.writeOptional(COMPONENT, displayName);
        writer.writeOptional(numberFormat);
    }

}
