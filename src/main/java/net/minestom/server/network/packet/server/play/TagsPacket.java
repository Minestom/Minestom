package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class TagsPacket implements ServerPacket {

    public List<Tag> blockTags = new LinkedList<>();
    public List<Tag> itemTags = new LinkedList<>();
    public List<Tag> fluidTags = new LinkedList<>();
    public List<Tag> entityTags = new LinkedList<>();

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
        writeTags(writer, blockTags, name -> Block.REGISTRY.fromNamespaceId(name).getBlockId());
        writeTags(writer, itemTags, name -> Registry.MATERIAL_REGISTRY.get(name).getNumericalId());
        writeTags(writer, fluidTags, name -> Registry.FLUID_REGISTRY.get(name).getNumericalId());
        writeTags(writer, entityTags, name -> Registry.ENTITY_TYPE_REGISTRY.get(name).getNumericalId()); // Not defaulted therefore nullable.
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        readTags(reader, blockTags, id -> Block.REGISTRY.fromStateId(id.shortValue()).getNamespaceId());
        readTags(reader, itemTags, id -> Registry.MATERIAL_REGISTRY.get(id.shortValue()).getId());
        readTags(reader, fluidTags, id -> Registry.FLUID_REGISTRY.get(id.shortValue()).getId());
        readTags(reader, entityTags, id -> Registry.ENTITY_TYPE_REGISTRY.get(id.shortValue()).getId()); // Not defaulted therefore nullable.
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

    public void readTags(BinaryReader reader, List<Tag> output, Function<Integer, NamespaceID> idSupplier) {
        output.clear();
        int length = reader.readVarInt();
        for (int i = 0; i < length; i++) {
            String name = reader.readSizedString(Integer.MAX_VALUE);

            int count = reader.readVarInt();
            Set<NamespaceID> values = new HashSet<>();
            for (int j = 0; j < count; j++) {
                int protocolID = reader.readVarInt();
                values.add(idSupplier.apply(protocolID));
            }

            output.add(new Tag(NamespaceID.from(name), values));
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
