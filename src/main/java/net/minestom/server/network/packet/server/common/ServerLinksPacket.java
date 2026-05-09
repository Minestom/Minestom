package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ServerLinksPacket(List<Entry> entries) implements ServerPacket.Configuration, ServerPacket.Play {
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

    public record Entry(@Nullable KnownLinkType knownType, @Nullable Component customType, String link) {
        private static final NetworkBuffer.Type<Entry> KNOWN_SERIALIZER = NetworkBufferTemplate.template(
                KnownLinkType.NETWORK_TYPE, Entry::knownType,
                NetworkBuffer.STRING, Entry::link,
                Entry::new
        );
        private static final NetworkBuffer.Type<Entry> CUSTOM_SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.COMPONENT, Entry::customType,
                NetworkBuffer.STRING, Entry::link,
                Entry::new
        );
        public static final NetworkBuffer.Type<Entry> NETWORK_TYPE = NetworkBuffer.Type.tagged(
                NetworkBuffer.BOOLEAN, entry -> entry.knownType != null,
                isKnown -> isKnown ? KNOWN_SERIALIZER : CUSTOM_SERIALIZER
        );

        public Entry {
            Check.argCondition(knownType == null && customType == null, "One of knownType and customType must be present");
            Check.argCondition(knownType != null && customType != null, "Only one of knownType and customType may be present");
        }

        public Entry(KnownLinkType type, String link) {
            this(type, null, link);
        }

        public Entry(Component type, String link) {
            this(null, type, link);
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
