package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.validate.Check;
import org.jspecify.annotations.Nullable;

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
        public static final NetworkBuffer.Type<Entry> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, Entry value) {
                buffer.write(NetworkBuffer.BOOLEAN, value.knownType != null);
                if (value.knownType != null) {
                    buffer.write(KnownLinkType.NETWORK_TYPE, value.knownType);
                } else {
                    assert value.customType != null;
                    buffer.write(NetworkBuffer.COMPONENT, value.customType);
                }
                buffer.write(NetworkBuffer.STRING, value.link);
            }

            @Override
            public Entry read(NetworkBuffer buffer) {
                boolean known = buffer.read(NetworkBuffer.BOOLEAN);
                if (known) {
                    return new Entry(buffer.read(KnownLinkType.NETWORK_TYPE), buffer.read(NetworkBuffer.STRING));
                } else {
                    return new Entry(buffer.read(NetworkBuffer.COMPONENT), buffer.read(NetworkBuffer.STRING));
                }
            }
        };

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
