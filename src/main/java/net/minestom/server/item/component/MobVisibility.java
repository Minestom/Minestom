package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.validate.Check;

/**
 * Scales how easily the listed mobs notice the wearer.
 *
 * @param targetingEntityTypes the mobs the scaling applies to
 * @param visibility           the scaling factor, between 0 and 10
 */
public record MobVisibility(RegistryTag<EntityType> targetingEntityTypes, float visibility) {

    public static final NetworkBuffer.Type<MobVisibility> NETWORK_TYPE = NetworkBufferTemplate.template(
            RegistryTag.networkType(Registries::entityType), MobVisibility::targetingEntityTypes,
            NetworkBuffer.FLOAT, MobVisibility::visibility,
            MobVisibility::new);
    public static final Codec<MobVisibility> CODEC = StructCodec.struct(
            "targeting_entity_types", RegistryTag.codec(Registries::entityType), MobVisibility::targetingEntityTypes,
            "visibility", Codec.FLOAT, MobVisibility::visibility,
            MobVisibility::new);

    public MobVisibility {
        Check.argCondition(visibility < 0 || visibility > 10, "Visibility must be between 0 and 10");
    }
}
