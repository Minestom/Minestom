package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagsPacket implements ServerPacket {
    private static final TagsPacket REQUIRED_TAGS_PACKET = new TagsPacket(MinecraftServer.getTagManager().getTagMap());

    public Map<Tag.BasicType, List<Tag>> tagsMap;

    public TagsPacket(Map<Tag.BasicType, List<Tag>> tagsMap) {
        this.tagsMap = tagsMap;
    }

    public TagsPacket() {
        this(new HashMap<>());
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
    public void read(@NotNull BinaryBuffer reader) {
        this.tagsMap = new HashMap<>();
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
