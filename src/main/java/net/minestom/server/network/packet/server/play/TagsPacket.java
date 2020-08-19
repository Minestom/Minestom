package net.minestom.server.network.packet.server.play;

import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class TagsPacket implements ServerPacket {

    public List<Tag> blockTags = new LinkedList<>();
    public List<Tag> itemTags = new LinkedList<>();
    public List<Tag> fluidTags = new LinkedList<>();
    public List<Tag> entityTags = new LinkedList<>();

    @Override
    public void write(BinaryWriter writer) {
        writeTags(writer, blockTags, name -> Registries.getBlock(name).ordinal());
        writeTags(writer, itemTags, name -> Registries.getMaterial(name).ordinal());
        writeTags(writer, fluidTags, name -> Registries.getFluid(name).ordinal());
        writeTags(writer, entityTags, name -> Registries.getEntityType(name).ordinal());
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
}
