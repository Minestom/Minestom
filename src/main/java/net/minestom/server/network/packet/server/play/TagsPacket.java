package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.fluid.Fluid;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
        this.tagsMap = new HashMap<>();
        // Read amount of tag types
        final int typeCount = reader.readVarInt();
        for (int i = 0; i < typeCount; i++) {
            // Read tag type
            final Tag.BasicTypes tagType = Tag.BasicTypes.fromIdentifer(reader.readSizedString(Integer.MAX_VALUE));
            if (tagType == null) {
                throw new IllegalArgumentException("Tag type could not be resolved");
            }
            switch (tagType) {
                case BLOCKS: {
                    // Read tag ID
                    final String identifier = reader.readSizedString(Integer.MAX_VALUE);
                    List<Tag> tags = new ArrayList<>();
                    // Read amount of tags
                    final int tagCount = reader.readVarInt();
                    Set<NamespaceID> values = new HashSet<>();
                    // Read tags
                    for (int j = 0; j < tagCount; j++) {
                        int protocolID = reader.readVarInt();
                        Block b = Block.fromId((short) protocolID);
                        if (b == null) {
                            throw new IllegalArgumentException("Block with id '" + protocolID + "' could not be resolved for a tag.");
                        } else {
                            values.add(NamespaceID.from(b.getName()));
                        }
                    }

                    tags.add(new Tag(NamespaceID.from(identifier), values));
                    this.tagsMap.put(Tag.BasicTypes.BLOCKS, tags);
                    break;
                }
                case ENTITY_TYPES: {
                    // Read tag ID
                    final String identifier = reader.readSizedString(Integer.MAX_VALUE);
                    List<Tag> tags = new ArrayList<>();
                    // Read amount of tags
                    final int tagCount = reader.readVarInt();
                    Set<NamespaceID> values = new HashSet<>();
                    // Read tags
                    for (int j = 0; j < tagCount; j++) {
                        int protocolID = reader.readVarInt();
                        EntityType et = EntityType.fromId((short) protocolID);
                        if (et == null) {
                            throw new IllegalArgumentException("Entity type with id '" + protocolID + "' could not be resolved for a tag.");
                        } else {
                            values.add(et.getNamespaceID());
                        }
                    }

                    tags.add(new Tag(NamespaceID.from(identifier), values));
                    this.tagsMap.put(Tag.BasicTypes.ENTITY_TYPES, tags);
                    break;
                }
                case FLUIDS: {
                    // Read tag ID
                    final String identifier = reader.readSizedString(Integer.MAX_VALUE);
                    List<Tag> tags = new ArrayList<>();
                    // Read amount of tags
                    final int tagCount = reader.readVarInt();
                    Set<NamespaceID> values = new HashSet<>();
                    // Read tags
                    for (int j = 0; j < tagCount; j++) {
                        int protocolID = reader.readVarInt();
                        Fluid f = Fluid.fromId((short) protocolID);
                        if (f == null) {
                            throw new IllegalArgumentException("Fluid with id '" + protocolID + "' could not be resolved for a tag.");
                        } else {
                            values.add(f.getNamespaceID());
                        }
                    }

                    tags.add(new Tag(NamespaceID.from(identifier), values));
                    this.tagsMap.put(Tag.BasicTypes.FLUIDS, tags);
                    break;
                }
                case GAME_EVENTS: {
                    // Read tag ID
                    final String identifier = reader.readSizedString(Integer.MAX_VALUE);
                    List<Tag> tags = new ArrayList<>();
                    // Read amount of tags
                    final int tagCount = reader.readVarInt();
                    Set<NamespaceID> values = new HashSet<>();
                    // Read tags
                    for (int j = 0; j < tagCount; j++) {
                        int protocolID = reader.readVarInt();
                        // TODO: GameEvent
//                        GameEvent ge = GameEvent.fromId((short) protocolID);
//                        if (ge == null) {
//                            throw new IllegalArgumentException("Game event with id '" + protocolID + "' could not be resolved for a tag.");
//                        } else {
//                            values.add(ge.getNamespaceID());
//                        }
                    }

                    tags.add(new Tag(NamespaceID.from(identifier), values));
                    this.tagsMap.put(Tag.BasicTypes.GAME_EVENTS, tags);
                    break;
                }
                case ITEMS: {
                    // Read tag ID
                    final String identifier = reader.readSizedString(Integer.MAX_VALUE);
                    List<Tag> tags = new ArrayList<>();
                    // Read amount of tags
                    final int tagCount = reader.readVarInt();
                    Set<NamespaceID> values = new HashSet<>();
                    // Read tags
                    for (int j = 0; j < tagCount; j++) {
                        int protocolID = reader.readVarInt();
                        Material m = Material.fromId((short) protocolID);
                        if (m == null) {
                            throw new IllegalArgumentException("Item with id '" + protocolID + "' could not be resolved for a tag.");
                        } else {
                            values.add(m.getNamespaceID());
                        }
                    }

                    tags.add(new Tag(NamespaceID.from(identifier), values));
                    this.tagsMap.put(Tag.BasicTypes.ITEMS, tags);
                    break;
                }
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
