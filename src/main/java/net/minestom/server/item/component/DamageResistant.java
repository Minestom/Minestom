package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;

public record DamageResistant(RegistryTag<DamageType> types) {
    public static final NetworkBuffer.Type<DamageResistant> NETWORK_TYPE = NetworkBufferTemplate.template(
            RegistryTag.networkType(Registries::damageType), DamageResistant::types,
            DamageResistant::new);
    public static final Codec<DamageResistant> CODEC = StructCodec.struct(
            "types", RegistryTag.codec(Registries::damageType), DamageResistant::types,
            DamageResistant::new);
}
