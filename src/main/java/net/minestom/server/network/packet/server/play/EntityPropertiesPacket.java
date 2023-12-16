package net.minestom.server.network.packet.server.play;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityPropertiesPacket(int entityId, List<AttributeInstance> properties) implements ServerPacket {
    public EntityPropertiesPacket {
        properties = List.copyOf(properties);
    }

    public EntityPropertiesPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.readCollection(r -> {
            final Attribute attribute = Attribute.fromKey(reader.read(STRING));
            final double value = reader.read(DOUBLE);
            int modifierCount = reader.read(VAR_INT);
            AttributeInstance instance = new AttributeInstance(attribute, null);
            for (int i = 0; i < modifierCount; i++) {
                AttributeModifier modifier = new AttributeModifier(reader.read(UUID), "", reader.read(DOUBLE), AttributeOperation.fromId(reader.read(BYTE)));
                instance.addModifier(modifier);
            }
            return instance;
        }));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(VAR_INT, properties.size());
        for (AttributeInstance instance : properties) {
            final Attribute attribute = instance.getAttribute();

            writer.write(STRING, attribute.key());
            writer.write(DOUBLE, (double) instance.getBaseValue());

            {
                Collection<AttributeModifier> modifiers = instance.getModifiers();
                writer.write(VAR_INT, modifiers.size());

                for (var modifier : modifiers) {
                    writer.write(UUID, modifier.getId());
                    writer.write(DOUBLE, modifier.getAmount());
                    writer.write(BYTE, (byte) modifier.getOperation().getId());
                }
            }
        }
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.ENTITY_PROPERTIES;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
