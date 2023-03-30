package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static net.minestom.server.network.NetworkBuffer.*;

public record TagsPacket(@NotNull Map<Tag.BasicType, List<Tag>> tagsMap) implements ServerPacket {
    @ApiStatus.Internal
    public static final CachedPacket DEFAULT_TAGS = new CachedPacket(new TagsPacket(MinecraftServer.getTagManager().getTagMap()));

    public TagsPacket {
        tagsMap = Map.copyOf(tagsMap);
    }

    public TagsPacket(@NotNull NetworkBuffer reader) {
        this(readTagsMap(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, tagsMap.size());
        for (var entry : tagsMap.entrySet()) {
            final var type = entry.getKey();
            final var tags = entry.getValue();
            writer.write(STRING, type.getIdentifier());
            writer.write(VAR_INT, tags.size());
            for (var tag : tags) {
                writer.write(STRING, tag.getName().asString());
                final var values = tag.getValues();
                writer.write(VAR_INT, values.size());
                for (var name : values) {
                    writer.write(VAR_INT, type.getFunction().apply(name.asString()));
                }
            }
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TAGS;
    }

    private static Map<Tag.BasicType, List<Tag>> readTagsMap(@NotNull NetworkBuffer reader) {
        Map<Tag.BasicType, List<Tag>> tagsMap = new EnumMap<>(Tag.BasicType.class);
        // Read amount of tag types
        final int typeCount = reader.read(VAR_INT);
        for (int i = 0; i < typeCount; i++) {
            // Read tag type
            final Tag.BasicType tagType = Tag.BasicType.fromIdentifer(reader.read(STRING));
            if (tagType == null) {
                throw new IllegalArgumentException("Tag type could not be resolved");
            }

            final int tagCount = reader.read(VAR_INT);
            for (int j = 0; j < tagCount; j++) {
                final String tagName = reader.read(STRING);
                final int[] entries = reader.read(VAR_INT_ARRAY);
                // TODO convert
            }
        }
        return tagsMap;
    }
}
