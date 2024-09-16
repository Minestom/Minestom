package net.minestom.server.item.component;

import net.kyori.adventure.nbt.*;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record PotionContents(
        @Nullable PotionType potion,
        @Nullable RGBLike customColor,
        @NotNull List<CustomPotionEffect> customEffects
) {
    public static final int POTION_DRINK_TIME = 32; // 32 ticks, in ms
    public static final PotionContents EMPTY = new PotionContents(null, null, List.of());

    public static final NetworkBuffer.Type<PotionContents> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, PotionContents value) {
            Integer typeId = value.potion == null ? null : value.potion.id();
            buffer.write(NetworkBuffer.VAR_INT.optional(), typeId);
            buffer.write(Color.NETWORK_TYPE.optional(), value.customColor);
            buffer.write(CustomPotionEffect.NETWORK_TYPE.list(), value.customEffects);
        }

        @Override
        public PotionContents read(@NotNull NetworkBuffer buffer) {
            Integer typeId = buffer.read(NetworkBuffer.VAR_INT.optional());
            return new PotionContents(
                    typeId == null ? null : PotionType.fromId(typeId),
                    buffer.read(Color.NETWORK_TYPE.optional()),
                    buffer.read(CustomPotionEffect.NETWORK_TYPE.list(Short.MAX_VALUE))
            );
        }
    };

    public static final BinaryTagSerializer<PotionContents> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull PotionContents value) {
            CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();

            if (value.potion != null) {
                builder.put("potion", StringBinaryTag.stringBinaryTag(value.potion.name()));
            }

            if (value.customColor != null) {
                builder.put("custom_color", Color.NBT_TYPE.write(value.customColor));
            }

            if (!value.customEffects.isEmpty()) {
                ListBinaryTag.Builder<BinaryTag> effectsBuilder = ListBinaryTag.builder();
                for (CustomPotionEffect effect : value.customEffects) {
                    effectsBuilder.add(CustomPotionEffect.NBT_TYPE.write(effect));
                }
                builder.put("custom_effects", effectsBuilder.build());
            }

            return builder.build();
        }

        @Override
        public @NotNull PotionContents read(@NotNull BinaryTag tag) {
            // Can be a string with just a potion effect id
            if (tag instanceof StringBinaryTag string) {
                return new PotionContents(PotionType.fromNamespaceId(string.value()), null, List.of());
            }

            // Otherwise must be a compound
            if (!(tag instanceof CompoundBinaryTag compound)) {
                return EMPTY;
            }

            PotionType potion = null;
            if (compound.get("potion") instanceof StringBinaryTag potionTag)
                potion = PotionType.fromNamespaceId(potionTag.value());

            Color customColor = null;
            if (compound.get("custom_color") instanceof IntBinaryTag colorTag) {
                customColor = new Color(colorTag.value());
            }

            List<CustomPotionEffect> customEffects = new ArrayList<>();
            ListBinaryTag customEffectsTag = compound.getList("custom_effects", BinaryTagTypes.COMPOUND);
            for (BinaryTag customEffectTag : customEffectsTag) {
                if (!(customEffectTag instanceof CompoundBinaryTag customEffectCompound)) {
                    continue;
                }
                customEffects.add(CustomPotionEffect.NBT_TYPE.read(customEffectCompound));
            }

            return new PotionContents(potion, customColor, customEffects);
        }
    };

    public PotionContents {
        customEffects = List.copyOf(customEffects);
    }

    public PotionContents(@NotNull PotionType potion) {
        this(potion, null, List.of());
    }

    public PotionContents(@NotNull PotionType potion, @NotNull RGBLike customColor) {
        this(potion, customColor, List.of());
    }

    public PotionContents(@NotNull List<CustomPotionEffect> customEffects) {
        this(null, null, customEffects);
    }

    public PotionContents(@NotNull CustomPotionEffect customEffect) {
        this(null, null, List.of(customEffect));
    }

}
