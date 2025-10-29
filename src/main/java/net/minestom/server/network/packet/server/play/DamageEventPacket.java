package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * See <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Damage_Event">the Minecraft wiki</a> for more info.
 *
 * @param targetEntityId ID of the entity being damaged
 * @param damageType     Type of damage type specified in {@link DamageType}
 * @param sourceEntityId null if there is no source entity, otherwise it is entityId
 * @param sourceDirectId null if there is no direct source. For direct attacks (e.g. melee), this is the same as sourceEntityId. For indirect attacks (e.g. projectiles), this is the projectile entity id
 * @param sourcePos      null if there is no source position, otherwise the position of the source
 */
public record DamageEventPacket(int targetEntityId, RegistryKey<DamageType> damageType,
                                @Nullable Integer sourceEntityId, @Nullable Integer sourceDirectId,
                                @Nullable Point sourcePos) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DamageEventPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, DamageEventPacket::targetEntityId,
            DamageType.NETWORK_TYPE, DamageEventPacket::damageType,
            OPTIONAL_VAR_INT, DamageEventPacket::sourceEntityId,
            OPTIONAL_VAR_INT, DamageEventPacket::sourceDirectId,
            VECTOR3D.optional(), DamageEventPacket::sourcePos,
            DamageEventPacket::new);
}
