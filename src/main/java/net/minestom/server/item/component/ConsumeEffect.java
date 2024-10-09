package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;

public record ConsumeEffect() {
    // TODO(1.21.2): APPLY_EFFECTS, REMOVE_EFFECTS, CLEAR_ALL_EFFECTS, TELEPORT_RANDOMLY, PLAY_SOUND
    public static final NetworkBuffer.Type<ConsumeEffect> NETWORK_TYPE = NetworkBufferTemplate.template(
            ConsumeEffect::new);
    public static final BinaryTagSerializer<ConsumeEffect> NBT_TYPE = BinaryTagSerializer.UNIT
            .map(ignored -> new ConsumeEffect(), effect -> null);
}
