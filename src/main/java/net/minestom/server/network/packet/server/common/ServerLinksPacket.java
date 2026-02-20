package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.Either;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public record ServerLinksPacket(List<Entry> entries) implements ServerPacket.Configuration, ServerPacket.Play, ServerPacket.ComponentHolding {
    private static final int MAX_ENTRIES = 100;

    public static final NetworkBuffer.Type<ServerLinksPacket> SERIALIZER = NetworkBufferTemplate.template(
            Entry.NETWORK_TYPE.list(MAX_ENTRIES), ServerLinksPacket::entries,
            ServerLinksPacket::new);

    public ServerLinksPacket {
        entries = List.copyOf(entries);
    }

    public ServerLinksPacket(Entry... entries) {
        this(List.of(entries));
    }

    @Override
    public @Unmodifiable Collection<Component> components() {
        if (entries.isEmpty()) return List.of();
        return entries.stream().map(Entry::components).flatMap(Collection::stream).toList();
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        if (entries.isEmpty()) return this;
        return new ServerLinksPacket(entries.stream().map(it -> it.copyWithOperator(operator)).toList());
    }

    public record Entry(Either<KnownLinkType, Component> payload, String link) implements ComponentHolder<Entry> {
        public static final NetworkBuffer.Type<Entry> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.Either(KnownLinkType.NETWORK_TYPE, NetworkBuffer.COMPONENT), Entry::payload,
                NetworkBuffer.STRING, Entry::link,
                Entry::new);

        public Entry(KnownLinkType type, String link) {
            this(Either.left(type), link);
        }

        public Entry(Component type, String link) {
            this(Either.right(type), link);
        }

        @Override
        public Collection<Component> components() {
            if (payload instanceof Either.Right(Component value)) return List.of(value);
            return List.of();
        }

        @Override
        public Entry copyWithOperator(UnaryOperator<Component> operator) {
            if (!(payload instanceof Either.Right(Component value))) return this;
            return new Entry(operator.apply(value), link);
        }
    }

    public enum KnownLinkType {
        BUG_REPORT,
        COMMUNITY_GUIDELINES,
        SUPPORT,
        STATUS,
        FEEDBACK,
        COMMUNITY,
        WEBSITE,
        FORUMS,
        NEWS,
        ANNOUNCEMENTS;

        public static final NetworkBuffer.Type<KnownLinkType> NETWORK_TYPE = NetworkBuffer.Enum(KnownLinkType.class);
    }
}
