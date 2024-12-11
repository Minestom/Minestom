package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.Nullable;

public record UseCooldown(float seconds, @Nullable String cooldownGroup) {
    public static final NetworkBuffer.Type<UseCooldown> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.FLOAT, UseCooldown::seconds,
            NetworkBuffer.STRING.optional(), UseCooldown::cooldownGroup,
            UseCooldown::new);
    public static final BinaryTagSerializer<UseCooldown> NBT_TYPE = BinaryTagSerializer.object(
            "seconds", BinaryTagSerializer.FLOAT, UseCooldown::seconds,
            "cooldown_group", BinaryTagSerializer.STRING.optional(), UseCooldown::cooldownGroup,
            UseCooldown::new);
}
