package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record Tool(List<Rule> rules, float defaultMiningSpeed, int damagePerBlock,
                   boolean canDestroyBlocksInCreative) {
    public static final float DEFAULT_MINING_SPEED = 1.0f;
    public static final int DEFAULT_DAMAGE_PER_BLOCK = 1;

    public static final NetworkBuffer.Type<Tool> NETWORK_TYPE = NetworkBufferTemplate.template(
            Rule.NETWORK_TYPE.list(Short.MAX_VALUE), Tool::rules,
            NetworkBuffer.FLOAT, Tool::defaultMiningSpeed,
            NetworkBuffer.VAR_INT, Tool::damagePerBlock,
            NetworkBuffer.BOOLEAN, Tool::canDestroyBlocksInCreative,
            Tool::new);
    public static final Codec<Tool> CODEC = StructCodec.struct(
            "rules", Rule.CODEC.list(), Tool::rules,
            "default_mining_speed", Codec.FLOAT.optional(DEFAULT_MINING_SPEED), Tool::defaultMiningSpeed,
            "damage_per_block", Codec.INT.optional(DEFAULT_DAMAGE_PER_BLOCK), Tool::damagePerBlock,
            "can_destroy_blocks_in_creative", Codec.BOOLEAN.optional(true), Tool::canDestroyBlocksInCreative,
            Tool::new);

    public record Rule(RegistryTag<Block> blocks, @Nullable Float speed, @Nullable Boolean correctForDrops) {

        public static final NetworkBuffer.Type<Rule> NETWORK_TYPE = NetworkBufferTemplate.template(
                RegistryTag.networkType(Registries::blocks), Rule::blocks,
                NetworkBuffer.FLOAT.optional(), Rule::speed,
                NetworkBuffer.BOOLEAN.optional(), Rule::correctForDrops,
                Rule::new
        );
        public static final Codec<Rule> CODEC = StructCodec.struct(
                "blocks", RegistryTag.codec(Registries::blocks), Rule::blocks,
                "speed", Codec.FLOAT.optional(), Rule::speed,
                "correct_for_drops", Codec.BOOLEAN.optional(), Rule::correctForDrops,
                Rule::new);
    }

    public boolean isCorrectForDrops(Block block) {
        for (Rule rule : rules) {
            if (rule.correctForDrops != null && rule.blocks.contains(block)) {
                // First matching rule is picked, other rules are ignored
                return rule.correctForDrops;
            }
        }
        return false;
    }

    public float getSpeed(Block block) {
        for (Rule rule : rules) {
            if (rule.speed != null && rule.blocks.contains(block)) {
                // First matching rule is picked, other rules are ignored
                return rule.speed;
            }
        }
        return defaultMiningSpeed;
    }
}
