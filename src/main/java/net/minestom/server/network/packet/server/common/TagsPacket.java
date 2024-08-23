package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.VAR_INT_ARRAY;

public record TagsPacket(List<Registry> registries) implements ServerPacket.Configuration, ServerPacket.Play {
    public TagsPacket {
        registries = List.copyOf(registries);
    }

    public static final NetworkBuffer.Type<TagsPacket> SERIALIZER = NetworkBufferTemplate.template(
            Registry.SERIALIZER.list(), TagsPacket::registries,
            TagsPacket::new
    );

    public record Registry(String registry, List<Tag> tags) {
        public static final NetworkBuffer.Type<Registry> SERIALIZER = NetworkBufferTemplate.template(
                STRING, Registry::registry,
                Tag.SERIALIZER.list(), Registry::tags,
                Registry::new
        );

        public Registry {
            tags = List.copyOf(tags);
        }
    }

    public record Tag(String identifier, int[] entries) {
        public static final NetworkBuffer.Type<Tag> SERIALIZER = NetworkBufferTemplate.template(
                STRING, Tag::identifier,
                VAR_INT_ARRAY, Tag::entries,
                Tag::new
        );
    }
}
