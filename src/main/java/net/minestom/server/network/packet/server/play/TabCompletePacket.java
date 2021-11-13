package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public record TabCompletePacket(int transactionId, int start, int length,
                                List<Match> matches) implements ComponentHoldingServerPacket {
    public TabCompletePacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readVarInt(), reader.readVarInt(), reader.readVarIntList(Match::new));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeVarInt(start);
        writer.writeVarInt(length);

        writer.writeVarInt(matches.size());
        for (Match match : matches) match.write(writer);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TAB_COMPLETE;
    }

    @Override
    public @NotNull Collection<Component> components() {
        if (matches == null || matches.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<Component> components = new ArrayList<>(matches.size());
            for (Match match : matches) {
                if (match.tooltip != null) {
                    components.add(match.tooltip);
                }
            }
            return components;
        }
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        if (matches == null || matches.isEmpty()) {
            return this;
        } else {
            var updatedMatches = new Match[matches.size()];
            for (int i = 0; i < updatedMatches.length; i++) {
                updatedMatches[i] = matches.get(i).copyWithOperator(operator);
            }
            return new TabCompletePacket(transactionId, start, length, List.of(updatedMatches));
        }
    }

    public record Match(String match, Component tooltip) implements Writeable, ComponentHolder<Match> {
        public Match(BinaryReader reader) {
            this(reader.readSizedString(), reader.readBoolean() ? reader.readComponent() : null);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(match);
            writer.writeBoolean(tooltip != null);
            if (tooltip != null) writer.writeComponent(tooltip);
        }

        @Override
        public @NotNull Collection<Component> components() {
            return tooltip != null ? Collections.singletonList(tooltip) : Collections.emptyList();
        }

        @Override
        public @NotNull Match copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return tooltip != null ? new Match(match, operator.apply(tooltip)) : this;
        }
    }
}
