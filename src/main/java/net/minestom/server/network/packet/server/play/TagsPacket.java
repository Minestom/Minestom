package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.tags.GameTag;
import net.minestom.server.tags.GameTagType;
import net.minestom.server.tags.GameTags;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record TagsPacket(@NotNull Map<GameTagType<?>, Set<GameTag<?>>> tagsMap) implements ServerPacket {
    @ApiStatus.Internal
    public static final CachedPacket DEFAULT_TAGS = new CachedPacket(new TagsPacket(GameTags.tags()));

    public TagsPacket {
        tagsMap = Map.copyOf(tagsMap);
    }

    public TagsPacket(BinaryReader reader) {
        this(readTagsMap(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(tagsMap.size());
        for (var entry : tagsMap.entrySet()) {
            final var type = entry.getKey();
            final var tags = entry.getValue();
            writer.writeSizedString(type.identifier().asString());
            writer.writeVarInt(tags.size());
            for (var tag : tags) {
                writer.writeSizedString(tag.name().asString());
                final var values = tag.values();
                writer.writeVarInt(values.size());
                for (var name : values) {
                    writer.writeVarInt(type.fromName().apply(name.namespace().asString()).id());
                }
            }
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TAGS;
    }

    @SuppressWarnings("unchecked")
    private static Map<GameTagType<?>, Set<GameTag<?>>> readTagsMap(BinaryReader reader) {
        final Map<GameTagType<?>, Set<GameTag<?>>> tagsMap = new HashMap<>();
        // Read amount of tag types
        final int typeCount = reader.readVarInt();
        for (int i = 0; i < typeCount; i++) {
            // Read tag type
            final GameTagType<ProtocolObject> tagType = (GameTagType<ProtocolObject>) GameTagType.fromIdentifier(reader.readSizedString());
            if (tagType == null) {
                throw new IllegalArgumentException("Tag type could not be resolved");
            }

            // Actually read the tags
            final int tagCount = reader.readVarInt();
            final Set<GameTag<?>> tags = new HashSet<>(tagCount);
            for (int j = 0; j < tagCount; j++) {
                final String tagName = reader.readSizedString();
                final int[] entries = reader.readVarIntArray();

                // We just use the base type here because generics are annoying
                final List<ProtocolObject> values = new ArrayList<>();
                for (final int entry : entries) {
                    values.add(tagType.fromId().apply(entry));
                }
                tags.add(new GameTag<>(NamespaceID.from(tagName), tagType, values));
            }

            tagsMap.put(tagType, tags);
        }
        return tagsMap;
    }
}
