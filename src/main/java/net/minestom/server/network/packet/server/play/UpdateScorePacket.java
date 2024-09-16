package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record UpdateScorePacket(
        @NotNull String entityName,
        @NotNull String objectiveName,
        int score,
        @Nullable Component displayName,
        @Nullable Sidebar.NumberFormat numberFormat
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<UpdateScorePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, UpdateScorePacket::entityName,
            STRING, UpdateScorePacket::objectiveName,
            VAR_INT, UpdateScorePacket::score,
            COMPONENT.optional(), UpdateScorePacket::displayName,
            Sidebar.NumberFormat.SERIALIZER.optional(), UpdateScorePacket::numberFormat,
            UpdateScorePacket::new
    );
}
