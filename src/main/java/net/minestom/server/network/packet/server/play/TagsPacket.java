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

import java.util.*;
import java.util.function.Function;

public class TagsPacket implements ServerPacket {

    public Map<Tag.BasicTypes, List<Tag>> tagsMap = new HashMap<>();

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
        for (Tag.BasicTypes type : Tag.BasicTypes.values()) {
            switch (type) {
                case BLOCKS: {
                    List<Tag> tags = tagsMap.get(type);

                    // Tag type
                    writer.writeSizedString(type.getIdentifier());
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
                            Block b = Registries.getBlock(name);
                            if (b == Block.AIR && !name.toString().equals("minecraft:air")) {
                                writer.writeVarInt(-1);
                            }
                            writer.writeVarInt(b.ordinal());
                        }
                    }
                    break;
                }
                case ENTITY_TYPES: {
                    List<Tag> tags = tagsMap.get(type);

                    // Tag type
                    writer.writeSizedString(type.getIdentifier());

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
                    List<Tag> tags = tagsMap.get(type);

                    // Tag type
                    writer.writeSizedString(type.getIdentifier());

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
                    List<Tag> tags = tagsMap.get(type);

                    // Tag type
                    writer.writeSizedString(type.getIdentifier());

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
                    List<Tag> tags = tagsMap.get(type);

                    // Tag type
                    writer.writeSizedString(type.getIdentifier());

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
                            writer.writeVarInt(Registries.getMaterial(name).ordinal());
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        // TODO: revamp this.
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
            // TODO: Convert identifier to TagType
//            this.tagsMap.put(identifier, tags);
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
