package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class TagsPacket implements ServerPacket {

    public Map<String, List<Tag>> tagsMap = new HashMap<>();

    private static final TagsPacket REQUIRED_TAGS_PACKET = new TagsPacket();

    static {
        MinecraftServer.getTagManager().addRequiredTagsToPacket(REQUIRED_TAGS_PACKET);
    }

    /**
     * Default constructor, required for reflection operations.
     */
    public TagsPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(tagsMap.size());
        tagsMap.forEach((s, tags) -> {
            writer.writeSizedString(s);

            writer.writeVarInt(tags.size());
            for (Tag tag : tags) {
                // name
                writer.writeSizedString(tag.getName().toString());

                final Set<NamespaceID> values = tag.getValues();
                // count
                writer.writeVarInt(values.size());
                // entries
                for (NamespaceID name : values) {
                    // TODO id from namespace
                    writer.writeVarInt(0);
                }
            }
        });
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.tagsMap = new HashMap<>();
        final int typeCount = reader.readVarInt();
        for (int i = 0; i < typeCount; i++) {
            final String identifier = reader.readSizedString(Integer.MAX_VALUE);
            List<Tag> tags = new ArrayList<>();
            final int tagCount = reader.readVarInt();
            Set<NamespaceID> values = new HashSet<>();
            for (int j = 0; j < tagCount; j++) {
                int protocolID = reader.readVarInt();
                // TODO tag from id
                values.add(null);
            }

            tags.add(new Tag(NamespaceID.from(identifier), values));
            this.tagsMap.put(identifier, tags);
        }
    }

    private void writeTags(BinaryWriter writer, List<Tag> tags, Function<NamespaceID, Integer> idSupplier) {
        writer.writeVarInt(tags.size());
        for (Tag tag : tags) {
            // name
            writer.writeSizedString(tag.getName().toString());

            final Set<NamespaceID> values = tag.getValues();
            // count
            writer.writeVarInt(values.size());
            // entries
            for (NamespaceID name : values) {
                writer.writeVarInt(idSupplier.apply(name));
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
    @NotNull
    public static TagsPacket getRequiredTagsPacket() {
        return REQUIRED_TAGS_PACKET;
    }
}
