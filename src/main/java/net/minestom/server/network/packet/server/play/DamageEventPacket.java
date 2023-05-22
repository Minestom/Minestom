package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

// Notes
// sourceEntityId - 0 indicates no source entity, otherwise it is entityId + 1
// sourceDirectId - 0 indicates no direct source. Direct attacks (e.g. melee) will have this number me the same as sourceEntityId, indirect attacks (e.g. projectiles) will have this be be the projectile entity id + 1
public record DamageEventPacket(int targetEntityId, int damageTypeId, int sourceEntityId, int sourceDirectId, @Nullable Vec sourcePos) implements ServerPacket {

    public DamageEventPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(VAR_INT), reader.read(VAR_INT), reader.read(VAR_INT),
            reader.read(BOOLEAN) ? new Vec(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE)) : null
        );
    }
    @Override
    public int getId() {
        return ServerPacketIdentifier.DAMAGE_EVENT;
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, targetEntityId);
        writer.write(VAR_INT, damageTypeId);
        writer.write(VAR_INT, sourceEntityId);
        writer.write(VAR_INT, sourceDirectId);
        if (sourcePos != null) {
            writer.write(BOOLEAN, true);
            writer.write(DOUBLE, sourcePos.x());
            writer.write(DOUBLE, sourcePos.y());
            writer.write(DOUBLE, sourcePos.z());
        } else {
            writer.write(BOOLEAN, false);
        }
    }
}
