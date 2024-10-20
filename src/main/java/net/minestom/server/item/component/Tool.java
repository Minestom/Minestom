package net.minestom.server.item.component;

import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.minestom.server.instance.block.predicate.BlockTypeFilter;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record Tool(@NotNull List<Rule> rules, float defaultMiningSpeed, int damagePerBlock) {
    public static final float DEFAULT_MINING_SPEED = 1.0f;
    public static final int DEFAULT_DAMAGE_PER_BLOCK = 1;

    public static final NetworkBuffer.Type<Tool> NETWORK_TYPE = NetworkBufferTemplate.template(
            Rule.NETWORK_TYPE.list(Short.MAX_VALUE), Tool::rules,
            NetworkBuffer.FLOAT, Tool::defaultMiningSpeed,
            NetworkBuffer.VAR_INT, Tool::damagePerBlock,
            Tool::new);
    public static final BinaryTagSerializer<Tool> NBT_TYPE = BinaryTagTemplate.object(
            "rules", Rule.NBT_TYPE.list(), Tool::rules,
            "default_mining_speed", BinaryTagSerializer.FLOAT.optional(DEFAULT_MINING_SPEED), Tool::defaultMiningSpeed,
            "damage_per_block", BinaryTagSerializer.INT.optional(DEFAULT_DAMAGE_PER_BLOCK), Tool::damagePerBlock,
            Tool::new);

    public record Rule(@NotNull BlockTypeFilter blocks, @Nullable Float speed, @Nullable Boolean correctForDrops) {

        public static final NetworkBuffer.Type<Rule> NETWORK_TYPE = NetworkBufferTemplate.template(
                BlockTypeFilter.NETWORK_TYPE, Rule::blocks,
                NetworkBuffer.FLOAT.optional(), Rule::speed,
                NetworkBuffer.BOOLEAN.optional(), Rule::correctForDrops,
                Rule::new
        );
        public static final BinaryTagSerializer<Rule> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> new Rule(
                        BlockTypeFilter.NBT_TYPE.read(Objects.requireNonNull(tag.get("blocks"))),
                        tag.get("speed") instanceof FloatBinaryTag speed ? speed.floatValue() : null,
                        tag.get("correct_for_drops") instanceof ByteBinaryTag correctForDrops ? correctForDrops.value() != 0 : null
                ),
                rule -> {
                    CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                    builder.put("blocks", BlockTypeFilter.NBT_TYPE.write(rule.blocks()));
                    if (rule.speed() != null) builder.putFloat("speed", rule.speed());
                    if (rule.correctForDrops() != null) builder.putBoolean("correct_for_drops", rule.correctForDrops());
                    return builder.build();
                }
        );
    }
}
