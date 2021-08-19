package net.minestom.server.network.packet.server.play;

import java.util.*;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.tags.Tag;
import net.minestom.server.tags.TagType;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class TagsPacket implements ServerPacket {
    private static final TagsPacket REQUIRED_TAGS_PACKET = new TagsPacket(MinecraftServer.getTagManager().getTags());

    public Map<TagType<?>, List<Tag<?>>> tagsMap;

    public TagsPacket(Map<TagType<?>, List<Tag<?>>> tagsMap) {
        this.tagsMap = tagsMap;
    }

    public TagsPacket() {
        this(new HashMap<>());
    }

    @Override
    public void write(final @NotNull BinaryWriter writer) {
        writer.writeVarInt(tagsMap.size());
        for (final var entry : tagsMap.entrySet()) {
            final var type = entry.getKey();
            final var tags = entry.getValue();
            writer.writeSizedString(type.identifier());
            writer.writeVarInt(tags.size());
            for (final var tag : tags) {
                writer.writeSizedString(tag.name().asString());
                final var values = tag.values();
                writer.writeVarInt(values.size());
                for (final var object : values) {
                    writer.writeVarInt(type.fromName().apply(object.namespace().asString()).id());
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(@NotNull BinaryReader reader) {
        this.tagsMap = new HashMap<>();
        // Read amount of tag types
        final int typeCount = reader.readVarInt();
        for (int i = 0; i < typeCount; i++) {
            // Read tag type
            final TagType<ProtocolObject> tagType = (TagType<ProtocolObject>) TagType.fromIdentifier(reader.readSizedString());
            if (tagType == null) {
                throw new IllegalArgumentException("Tag type could not be resolved");
            }

            final int tagCount = reader.readVarInt();
            final List<Tag<?>> tags = new ArrayList<>();
            for (int j = 0; j < tagCount; j++) {
                final String tagName = reader.readSizedString();
                final int[] entries = reader.readVarIntArray();
                final List<ProtocolObject> values = new ArrayList<>();
                for (final int entry : entries) {
                    values.add(tagType.fromId().apply(entry));
                }
                tags.add(new Tag<>(NamespaceID.from(tagName), tagType, values));
            }
            tagsMap.put(tagType, tags);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TAGS;
    }

    /**
     * Gets the {@link TagsPacket} sent to every {@link net.minestom.server.entity.Player}
     * on login.
     *
     * @return the default tags packet
     */
    public static @NotNull TagsPacket getRequiredTagsPacket() {
        return REQUIRED_TAGS_PACKET;
    }
}
