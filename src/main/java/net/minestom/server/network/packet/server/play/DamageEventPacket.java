package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

/**
 * See <a href="https://wiki.vg/Protocol#Damage_Event">https://wiki.vg/Protocol#Damage_Event</a> for more info.
 *
 * @param targetEntityId ID of the entity being damaged
 * @param damageTypeId   ID of damage type
 * @param sourceEntityId 0 if there is no source entity, otherwise it is entityId + 1
 * @param sourceDirectId 0 if there is no direct source. For direct attacks (e.g. melee), this is the same as sourceEntityId. For indirect attacks (e.g. projectiles), this is the projectile entity id + 1
 * @param sourcePos      null if there is no source position, otherwise the position of the source
 */
public record DamageEventPacket(int targetEntityId, int damageTypeId, int sourceEntityId, int sourceDirectId,
                                @Nullable Point sourcePos) implements ServerPacket.Play {

    public DamageEventPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(VAR_INT), reader.read(VAR_INT), reader.read(VAR_INT), reader.readOptional(VECTOR3D));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, targetEntityId);
        writer.write(VAR_INT, damageTypeId);
        writer.write(VAR_INT, sourceEntityId);
        writer.write(VAR_INT, sourceDirectId);
        writer.writeOptional(VECTOR3D, sourcePos);
    }
}
