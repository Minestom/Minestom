package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DeathProtection(@NotNull List<ConsumeEffect> deathEffects) {
    public static final NetworkBuffer.Type<DeathProtection> NETWORK_TYPE = NetworkBufferTemplate.template(
            ConsumeEffect.NETWORK_TYPE.list(256), DeathProtection::deathEffects,
            DeathProtection::new);
    public static final BinaryTagSerializer<DeathProtection> NBT_TYPE = BinaryTagTemplate.object(
            "death_effects", ConsumeEffect.NBT_TYPE.list(), DeathProtection::deathEffects,
            DeathProtection::new);
}
