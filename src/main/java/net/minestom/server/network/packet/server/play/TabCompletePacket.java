package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
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

    public TabCompletePacket {
        matches = List.copyOf(matches);
    }

    public TabCompletePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(VAR_INT), reader.read(VAR_INT), reader.readCollection(Match::new, MAX_ENTRIES));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, transactionId);
        writer.write(VAR_INT, start);
        writer.write(VAR_INT, length);
        writer.writeCollection(matches);
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

    public record Match(@NotNull String match,
                        @Nullable Component tooltip) implements NetworkBuffer.Writer, ComponentHolder<Match> {
        public Match(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), reader.read(BOOLEAN) ? reader.read(COMPONENT) : null);
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, match);
            writer.writeOptional(COMPONENT, tooltip);
        }

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
