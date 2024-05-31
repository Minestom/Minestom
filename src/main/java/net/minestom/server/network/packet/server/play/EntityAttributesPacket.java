package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityAttributesPacket(int entityId, List<AttributeInstance> attributes) implements ServerPacket.Play {
    public static final int MAX_ENTRIES = 1024;

    public EntityAttributesPacket {
        attributes = List.copyOf(attributes);
    }

    public EntityAttributesPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.readCollection(AttributeInstance.NETWORK_TYPE, MAX_ENTRIES));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.writeCollection(AttributeInstance.NETWORK_TYPE, attributes);
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.ENTITY_ATTRIBUTES;
    }
}
