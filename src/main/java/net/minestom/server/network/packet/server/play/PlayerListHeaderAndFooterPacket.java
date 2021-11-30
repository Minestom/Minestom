package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public record PlayerListHeaderAndFooterPacket(Component header,
                                              Component footer) implements ComponentHoldingServerPacket {
    public PlayerListHeaderAndFooterPacket(BinaryReader reader) {
        this(reader.readComponent(), reader.readComponent());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(Objects.requireNonNullElseGet(header, Component::empty));
        writer.writeComponent(Objects.requireNonNullElseGet(footer, Component::empty));
    }

    @Override
    public @NotNull Collection<Component> components() {
        List<Component> components = new ArrayList<>();
        if (header != null) {
            components.add(header);
        }
        if (footer != null) {
            components.add(footer);
        }
        return components;
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new PlayerListHeaderAndFooterPacket(header == null ? null : operator.apply(header), footer == null ? null : operator.apply(footer));
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_LIST_HEADER_AND_FOOTER;
    }
}
