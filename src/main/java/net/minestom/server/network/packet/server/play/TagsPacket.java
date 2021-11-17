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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TagsPacket(Map<Tag.BasicType, List<Tag>> tagsMap) implements ServerPacket {
    @ApiStatus.Internal
    public static final CachedPacket DEFAULT_TAGS = new CachedPacket(new TagsPacket(MinecraftServer.getTagManager().getTagMap()));

    public TagsPacket {
        tagsMap = Map.copyOf(tagsMap);
    }

    public TagsPacket(BinaryReader reader) {
        this(new HashMap<>()); // TODO
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
}
