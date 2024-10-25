package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;

public record DamageResistant(@NotNull String tagKey) {
    public static final NetworkBuffer.Type<DamageResistant> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, DamageResistant::tagKey,
            DamageResistant::new);
    public static final BinaryTagSerializer<DamageResistant> NBT_TYPE = BinaryTagTemplate.object(
            "types", BinaryTagSerializer.STRING, DamageResistant::tagKey,
            DamageResistant::new);
}
