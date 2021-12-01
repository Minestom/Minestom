package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record TagsPacket(@NotNull Map<Tag.BasicType, List<Tag>> tagsMap) implements ServerPacket {
    @ApiStatus.Internal
    public static final CachedPacket DEFAULT_TAGS = new CachedPacket(new TagsPacket(MinecraftServer.getTagManager().getTagMap()));

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
            writer.writeSizedString(type.getIdentifier());
            writer.writeVarInt(tags.size());
            for (var tag : tags) {
                writer.writeSizedString(tag.getName().asString());
                final var values = tag.getValues();
                writer.writeVarInt(values.size());
                for (var name : values) {
                    writer.writeVarInt(type.getFunction().apply(name.asString()));
                }
            }
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TAGS;
    }

    private static Map<Tag.BasicType, List<Tag>> readTagsMap(BinaryReader reader) {
        Map<Tag.BasicType, List<Tag>> tagsMap = new EnumMap<>(Tag.BasicType.class);
        // Read amount of tag types
        final int typeCount = reader.readVarInt();
        for (int i = 0; i < typeCount; i++) {
            // Read tag type
            final Tag.BasicType tagType = Tag.BasicType.fromIdentifer(reader.readSizedString());
            if (tagType == null) {
                throw new IllegalArgumentException("Tag type could not be resolved");
            }

            final int tagCount = reader.readVarInt();
            for (int j = 0; j < tagCount; j++) {
                final String tagName = reader.readSizedString();
                final int[] entries = reader.readVarIntArray();
                // TODO convert
            }
        }
        return tagsMap;
    }
}
