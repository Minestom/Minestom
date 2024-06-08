package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Food(int nutrition, float saturationModifier, boolean canAlwaysEat, float eatSeconds, @NotNull List<EffectChance> effects) {
    public static final float DEFAULT_EAT_SECONDS = 1.6f;

    public static final NetworkBuffer.Type<Food> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Food value) {
            buffer.write(NetworkBuffer.VAR_INT, value.nutrition);
            buffer.write(NetworkBuffer.FLOAT, value.saturationModifier);
            buffer.write(NetworkBuffer.BOOLEAN, value.canAlwaysEat);
            buffer.write(NetworkBuffer.FLOAT, value.eatSeconds);
            buffer.writeCollection(EffectChance.NETWORK_TYPE, value.effects);
        }

        @Override
        public Food read(@NotNull NetworkBuffer buffer) {
            return new Food(
                    buffer.read(NetworkBuffer.VAR_INT),
                    buffer.read(NetworkBuffer.FLOAT),
                    buffer.read(NetworkBuffer.BOOLEAN),
                    buffer.read(NetworkBuffer.FLOAT),
                    buffer.readCollection(EffectChance.NETWORK_TYPE, Short.MAX_VALUE)
            );
        }
    };
    public static final BinaryTagSerializer<Food> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new Food(
                    tag.getInt("nutrition"),
                    tag.getFloat("saturation_modifier"),
                    tag.getBoolean("can_always_eat"),
                    tag.getFloat("eat_seconds", DEFAULT_EAT_SECONDS),
                    EffectChance.NBT_LIST_TYPE.read(tag.getList("effects", BinaryTagTypes.COMPOUND))),
            value -> CompoundBinaryTag.builder()
                    .putInt("nutrition", value.nutrition)
                    .putFloat("saturationModifier", value.saturationModifier)
                    .putBoolean("canAlwaysEat", value.canAlwaysEat)
                    .putFloat("eatSeconds", value.eatSeconds)
                    .put("effects", EffectChance.NBT_LIST_TYPE.write(value.effects))
                    .build()
    );

    public Food {
        effects = List.copyOf(effects);
    }

    public int eatDurationTicks() {
        return (int) (eatSeconds * ServerFlag.SERVER_TICKS_PER_SECOND);
    }

    public record EffectChance(@NotNull CustomPotionEffect effect, float probability) {
        public static final NetworkBuffer.Type<EffectChance> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, EffectChance value) {

            }

            @Override
            public EffectChance read(@NotNull NetworkBuffer buffer) {
                return null;
            }
        };
        public static final BinaryTagSerializer<EffectChance> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> new EffectChance(
                        CustomPotionEffect.NBT_TYPE.read(tag.getCompound("effect")),
                        tag.getFloat("probability", 1f)),
                value -> CompoundBinaryTag.builder()
                        .put("effect", CustomPotionEffect.NBT_TYPE.write(value.effect()))
                        .putFloat("probability", value.probability)
                        .build()
        );
        public static final BinaryTagSerializer<List<EffectChance>> NBT_LIST_TYPE = NBT_TYPE.list();
    }

}
