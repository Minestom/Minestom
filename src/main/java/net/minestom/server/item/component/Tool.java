package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.minestom.server.instance.block.predicate.BlockTypeFilter;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record Tool(@NotNull List<Rule> rules, float defaultMiningSpeed, int damagePerBlock) {
    public static final float DEFAULT_MINING_SPEED = 1.0f;
    public static final int DEFAULT_DAMAGE_PER_BLOCK = 1;

    public static final NetworkBuffer.Type<Tool> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        private static final NetworkBuffer.Type<List<Rule>> RULE_LIST_TYPE = Rule.NETWORK_TYPE.list(Short.MAX_VALUE);

        @Override
        public void write(@NotNull NetworkBuffer buffer, Tool value) {
            RULE_LIST_TYPE.write(buffer, value.rules());
            buffer.write(NetworkBuffer.FLOAT, value.defaultMiningSpeed());
            buffer.write(NetworkBuffer.VAR_INT, value.damagePerBlock());
        }

        @Override
        public Tool read(@NotNull NetworkBuffer buffer) {
            return new Tool(
                    RULE_LIST_TYPE.read(buffer),
                    buffer.read(NetworkBuffer.FLOAT),
                    buffer.read(NetworkBuffer.VAR_INT)
            );
        }
    };
    public static final BinaryTagSerializer<Tool> NBT_TYPE = new BinaryTagSerializer<>() {
        private static final BinaryTagSerializer<List<Rule>> RULE_LIST_TYPE = Rule.NBT_TYPE.list();

        @Override
        public @NotNull BinaryTag write(@NotNull Tool value) {
            return CompoundBinaryTag.builder()
                    .put("rules", RULE_LIST_TYPE.write(value.rules()))
                    .putFloat("default_mining_speed", value.defaultMiningSpeed())
                    .putInt("damage_per_block", value.damagePerBlock())
                    .build();
        }

        @Override
        public @NotNull Tool read(@NotNull BinaryTag tag) {
            if (!(tag instanceof CompoundBinaryTag compound))
                throw new IllegalArgumentException("Expected a compound tag, got " + tag.type());
            return new Tool(
                    RULE_LIST_TYPE.read(Objects.requireNonNull(compound.get("rules"))),
                    compound.getFloat("default_mining_speed", DEFAULT_MINING_SPEED),
                    compound.getInt("damage_per_block", DEFAULT_DAMAGE_PER_BLOCK)
            );
        }
    };

    public record Rule(@NotNull BlockTypeFilter blocks, @Nullable Float speed, @Nullable Boolean correctForDrops) {

        public static final NetworkBuffer.Type<Rule> NETWORK_TYPE = NetworkBufferTemplate.template(
                BlockTypeFilter.NETWORK_TYPE, Rule::blocks,
                NetworkBuffer.FLOAT, Rule::speed,
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
