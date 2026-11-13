package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.validate.Check;

import java.util.List;

/**
 * Sets the post processing effects the client applies.
 *
 * @param postEffects the effects in the order they are applied
 */
public record PostEffectsPacket(List<Key> postEffects) implements ServerPacket.Configuration, ServerPacket.Play {
    private static final int MAX_ENTRIES = 256;

    public static final NetworkBuffer.Type<PostEffectsPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.KEY.list(MAX_ENTRIES), PostEffectsPacket::postEffects,
            PostEffectsPacket::new);

    public PostEffectsPacket {
        postEffects = List.copyOf(postEffects);
        Check.argCondition(postEffects.size() > MAX_ENTRIES, "At most " + MAX_ENTRIES + " post effects are allowed");
    }
}
