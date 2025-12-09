package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public record AttackRange(
        float minReach,
        float maxReach,
        float minCreativeReach,
        float maxCreativeReach,
        float hitboxMargin,
        float mobFactor
) {
    public static final NetworkBuffer.Type<AttackRange> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.FLOAT, AttackRange::minReach,
            NetworkBuffer.FLOAT, AttackRange::maxReach,
            NetworkBuffer.FLOAT, AttackRange::minCreativeReach,
            NetworkBuffer.FLOAT, AttackRange::maxCreativeReach,
            NetworkBuffer.FLOAT, AttackRange::hitboxMargin,
            NetworkBuffer.FLOAT, AttackRange::mobFactor,
            AttackRange::new);
    public static final Codec<AttackRange> CODEC = StructCodec.struct(
            "min_reach", Codec.FLOAT.optional(0f), AttackRange::minReach,
            "max_reach", Codec.FLOAT.optional(3f), AttackRange::maxReach,
            "min_creative_reach", Codec.FLOAT.optional(0f), AttackRange::minCreativeReach,
            "max_creative_reach", Codec.FLOAT.optional(5f), AttackRange::maxCreativeReach,
            "hitbox_margin", Codec.FLOAT.optional(0.3f), AttackRange::hitboxMargin,
            "mob_factor", Codec.FLOAT.optional(1f), AttackRange::mobFactor,
            AttackRange::new);

    public float effectiveMinReach(Entity entity) {
        if (!(entity instanceof Player player))
            return minReach * mobFactor;
        return switch (player.getGameMode()) {
            case SPECTATOR -> 0f;
            case CREATIVE -> minCreativeReach;
            default -> minReach;
        };
    }

    public float effectiveMaxReach(Entity entity) {
        if (!(entity instanceof Player player))
            return maxReach * mobFactor;
        return player.getGameMode() == GameMode.CREATIVE
                ? maxCreativeReach : maxReach;
    }
}
