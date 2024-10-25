package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record TabCompletePacket(int transactionId, int start, int length,
                                @NotNull List<Match> matches) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final int MAX_ENTRIES = Short.MAX_VALUE;

    public static final NetworkBuffer.Type<TabCompletePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, TabCompletePacket::transactionId,
            VAR_INT, TabCompletePacket::start,
            VAR_INT, TabCompletePacket::length,
            Match.SERIALIZER.list(MAX_ENTRIES), TabCompletePacket::matches,
            TabCompletePacket::new);

    public TabCompletePacket {
        matches = List.copyOf(matches);
    }

    @Override
    public @NotNull Collection<Component> components() {
        if (matches.isEmpty()) return List.of();
        List<Component> components = new ArrayList<>(matches.size());
        for (Match match : matches) {
            if (match.tooltip != null) {
                components.add(match.tooltip);
            }
        }
        return components;
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        if (matches.isEmpty()) return this;
        final List<Match> updatedMatches = matches.stream().map(match -> match.copyWithOperator(operator)).toList();
        return new TabCompletePacket(transactionId, start, length, updatedMatches);
    }

    public record Match(@NotNull String match, @Nullable Component tooltip) implements ComponentHolder<Match> {
        public static final NetworkBuffer.Type<Match> SERIALIZER = NetworkBufferTemplate.template(
                STRING, Match::match,
                COMPONENT.optional(), Match::tooltip,
                Match::new);

        @Override
        public @NotNull Collection<Component> components() {
            return tooltip != null ? List.of(tooltip) : List.of();
        }

        @Override
        public @NotNull Match copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return tooltip != null ? new Match(match, operator.apply(tooltip)) : this;
        }
    }
}
