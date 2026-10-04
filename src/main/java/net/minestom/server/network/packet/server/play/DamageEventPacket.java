package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.OPTIONAL_VAR_INT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

/**
 * See <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Damage_Event">the Minecraft wiki</a> for more info.
 *
 * @param targetEntityId ID of the entity being damaged
 * @param damageType     the damage type
 * @param sourceEntityId the ID of the entity responsible for the damage, or {@code null} when there is none
 * @param sourceDirectId the ID of the entity that directly dealt the damage, such as a projectile, or {@code null}
 *                       when there is none. For a melee hit this is the same as {@code sourceEntityId}
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
