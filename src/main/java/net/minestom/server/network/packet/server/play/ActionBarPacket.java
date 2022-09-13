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

public record ActionBarPacket(@NotNull Component text) implements ComponentHoldingServerPacket {
    public ActionBarPacket(BinaryReader reader) {
        this(reader.readComponent());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(text);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ACTION_BAR;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.text);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new ActionBarPacket(operator.apply(this.text));
    }
}
