package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerListHeaderAndFooterPacket implements ServerPacket {
    private static final String EMPTY_COMPONENT = GsonComponentSerializer.gson().serialize(Component.empty());

    public String header;
    public String footer;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(Objects.requireNonNullElse(header, EMPTY_COMPONENT));
        writer.writeSizedString(Objects.requireNonNullElse(footer, EMPTY_COMPONENT));
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_LIST_HEADER_AND_FOOTER;
    }
}
