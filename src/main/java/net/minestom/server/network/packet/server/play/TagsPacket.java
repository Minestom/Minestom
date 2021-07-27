package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagsPacket implements ServerPacket {

    private static final TagsPacket REQUIRED_TAGS_PACKET = new TagsPacket();

    static {
        MinecraftServer.getTagManager().addRequiredTagsToPacket(REQUIRED_TAGS_PACKET);
    }

    public Map<Tag.BasicTypes, List<Tag>> tagsMap = new HashMap<>();

    /**
     * Default constructor, required for reflection operations.
     */
    public TagsPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(tagsMap.size());
        for (var entry : tagsMap.entrySet()) {
            final var type = entry.getKey();
            final var tags = entry.getValue();
            // Tag type
            writer.writeSizedString(type.getIdentifier());
            switch (type) {
                case BLOCKS: {
                    // Number of tags
                    writer.writeVarInt(tags.size());
                    for (Tag tag : tags) {
                        // name
                        writer.writeSizedString(tag.getName().toString());
                        final Set<NamespaceID> values = tag.getValues();
                        // count
                        writer.writeVarInt(values.size());
                        // entries
                        for (NamespaceID name : values) {
                            Block b = Block.fromNamespaceId(name);
                            if (b == null) {
                                writer.writeVarInt(-1);
                                continue;
                            }
                            writer.writeVarInt(b.id());
                        }
                    }
                    break;
                }
                case ENTITY_TYPES: {
                    // Number of tags
                    writer.writeVarInt(tags.size());
                    for (Tag tag : tags) {
                        // name
                        writer.writeSizedString(tag.getName().toString());

                        final Set<NamespaceID> values = tag.getValues();
                        // count
                        writer.writeVarInt(values.size());
                        // entries
                        for (NamespaceID name : values) {
                            EntityType et = Registries.getEntityType(name);
                            if (et == null) {
                                writer.writeVarInt(-1);
                            } else {
                                writer.writeVarInt(et.ordinal());
                            }
                        }
                    }
                    break;
                }
                case FLUIDS: {
                    // Number of tags
                    writer.writeVarInt(tags.size());
                    for (Tag tag : tags) {
                        // name
                        writer.writeSizedString(tag.getName().toString());

                        final Set<NamespaceID> values = tag.getValues();
                        // count
                        writer.writeVarInt(values.size());
                        // entries
                        for (NamespaceID name : values) {
                            writer.writeVarInt(Registries.getFluid(name).ordinal());
                        }
                    }
                    break;
                }
                case GAME_EVENTS: {
                    // Number of tags
                    writer.writeVarInt(tags.size());
                    for (Tag tag : tags) {
                        // name
                        writer.writeSizedString(tag.getName().toString());

                        final Set<NamespaceID> values = tag.getValues();
                        // count
                        writer.writeVarInt(values.size());
                        // entries
                        for (NamespaceID name : values) {
                            // TODO: GameEvents
                            writer.writeVarInt(-1);
                        }
                    }
                    break;
                }
                case ITEMS: {
                    // Number of tags
                    writer.writeVarInt(tags.size());
                    for (Tag tag : tags) {
                        // name
                        writer.writeSizedString(tag.getName().toString());

                        final Set<NamespaceID> values = tag.getValues();
                        // count
                        writer.writeVarInt(values.size());
                        // entries
                        for (NamespaceID name : values) {
                            writer.writeVarInt(Registries.getMaterial(name).id());
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.tagsMap = new HashMap<>();
        // Read amount of tag types
        final int typeCount = reader.readVarInt();
        for (int i = 0; i < typeCount; i++) {
            // Read tag type
            final Tag.BasicTypes tagType = Tag.BasicTypes.fromIdentifer(reader.readSizedString());
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
    @NotNull
    public static TagsPacket getRequiredTagsPacket() {
        return REQUIRED_TAGS_PACKET;
    }
}
