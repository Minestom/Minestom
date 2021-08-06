package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public class TabCompletePacket implements ComponentHoldingServerPacket {

    public int transactionId;
    public int start;
    public int length;
    public Match[] matches;

    public TabCompletePacket() {
        matches = new Match[0];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeVarInt(start);
        writer.writeVarInt(length);

        writer.writeVarInt(matches.length);
        for (Match match : matches) {
            writer.writeSizedString(match.match);
            writer.writeBoolean(match.hasTooltip);
            if (match.hasTooltip)
                writer.writeComponent(match.tooltip);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        transactionId = reader.readVarInt();
        start = reader.readVarInt();
        length = reader.readVarInt();

        int matchCount = reader.readVarInt();
        matches = new Match[matchCount];
        for (int i = 0; i < matchCount; i++) {
            String match = reader.readSizedString();
            boolean hasTooltip = reader.readBoolean();
            Component tooltip = null;
            if (hasTooltip) {
                tooltip = reader.readComponent();
            }
            Match newMatch = new Match();
            newMatch.match = match;
            newMatch.hasTooltip = hasTooltip;
            newMatch.tooltip = tooltip;
            matches[i] = newMatch;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TAB_COMPLETE;
    }

    @Override
    public @NotNull Collection<Component> components() {
        if (matches == null || matches.length == 0) {
            return Collections.emptyList();
        } else {
            List<Component> components = new ArrayList<>(matches.length);
            for (Match match : matches) {
                if (match.hasTooltip) {
                    components.add(match.tooltip);
                }
            }
            return components;
        }
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        if (matches == null || matches.length == 0) {
            return this;
        } else {
            TabCompletePacket packet = new TabCompletePacket();
            packet.transactionId = transactionId;
            packet.start = start;
            packet.length = length;
            packet.matches = new Match[matches.length];

            for (int i = 0; i < matches.length; i++) {
                packet.matches[i] = matches[i].copyWithOperator(operator);
            }

            return packet;
        }
    }

    public static class Match implements ComponentHolder<Match> {
        public String match;
        public boolean hasTooltip;
        public Component tooltip;

        @Override
        public @NotNull Collection<Component> components() {
            if (hasTooltip) {
                return Collections.singleton(tooltip);
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public @NotNull Match copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            if (hasTooltip) {
                Match newMatch = new Match();
                newMatch.match = match;
                newMatch.hasTooltip = hasTooltip;
                newMatch.tooltip = tooltip;
                return newMatch;
            } else {
                return this;
            }
        }
    }

}
