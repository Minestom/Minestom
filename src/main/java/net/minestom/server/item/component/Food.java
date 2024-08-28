package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.ServerFlag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record Food(int nutrition, float saturationModifier, boolean canAlwaysEat, float eatSeconds,
                   @NotNull ItemStack usingConvertsTo, @NotNull List<EffectChance> effects) {
    public static final float DEFAULT_EAT_SECONDS = 1.6f;

    public static final NetworkBuffer.Type<Food> NETWORK_TYPE = NetworkBufferTemplate.template(
            VAR_INT, Food::nutrition,
            FLOAT, Food::saturationModifier,
            BOOLEAN, Food::canAlwaysEat,
            FLOAT, Food::eatSeconds,
            ItemStack.NETWORK_TYPE, Food::usingConvertsTo,
            EffectChance.NETWORK_TYPE.list(Short.MAX_VALUE), Food::effects,
            Food::new
    );

    public static final BinaryTagSerializer<Food> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new Food(
                    tag.getInt("nutrition"),
                    tag.getFloat("saturation_modifier"),
                    tag.getBoolean("can_always_eat"),
                    tag.getFloat("eat_seconds", DEFAULT_EAT_SECONDS),
                    tag.get("using_converts_to") instanceof BinaryTag usingConvertsTo
                            ? ItemStack.NBT_TYPE.read(usingConvertsTo) : ItemStack.AIR,
                    EffectChance.NBT_LIST_TYPE.read(tag.getList("effects", BinaryTagTypes.COMPOUND))),
            value -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder()
                        .putInt("nutrition", value.nutrition)
                        .putFloat("saturation_odifier", value.saturationModifier)
                        .putBoolean("can_always_eat", value.canAlwaysEat)
                        .putFloat("eat_seconds", value.eatSeconds)
                        .put("effects", EffectChance.NBT_LIST_TYPE.write(value.effects));
                if (!value.usingConvertsTo.isAir()) {
                    builder.put("using_converts_to", ItemStack.NBT_TYPE.write(value.usingConvertsTo));
                }
                return builder.build();
            }
    );

    public Food {
        effects = List.copyOf(effects);
    }

    public int eatDurationTicks() {
        return (int) (eatSeconds * ServerFlag.SERVER_TICKS_PER_SECOND);
    }

    public record EffectChance(@NotNull CustomPotionEffect effect, float probability) {
        public static final NetworkBuffer.Type<EffectChance> NETWORK_TYPE = NetworkBufferTemplate.template(
                CustomPotionEffect.NETWORK_TYPE, EffectChance::effect,
                FLOAT, EffectChance::probability,
                EffectChance::new
        );

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
