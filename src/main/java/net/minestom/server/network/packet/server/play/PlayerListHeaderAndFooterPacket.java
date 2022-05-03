package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public record PlayerListHeaderAndFooterPacket(@NotNull Component header,
                                              @NotNull Component footer) implements ComponentHoldingServerPacket {
    public PlayerListHeaderAndFooterPacket(BinaryReader reader) {
        this(reader.readComponent(), reader.readComponent());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(header);
        writer.writeComponent(footer);
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(header, footer);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new PlayerListHeaderAndFooterPacket(operator.apply(header), operator.apply(footer));
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_LIST_HEADER_AND_FOOTER;
    }
}
