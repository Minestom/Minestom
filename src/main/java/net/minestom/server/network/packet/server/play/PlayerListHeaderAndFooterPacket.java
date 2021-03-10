package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PlayerListHeaderAndFooterPacket implements ServerPacket {
    public Component header;
    public Component footer;

    public PlayerListHeaderAndFooterPacket(@Nullable Component header, @Nullable Component footer) {
        this.header = header;
        this.footer = footer;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(Objects.requireNonNullElse(header, Component.empty()));
        writer.writeComponent(Objects.requireNonNullElse(footer, Component.empty()));
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_LIST_HEADER_AND_FOOTER;
    }
}
