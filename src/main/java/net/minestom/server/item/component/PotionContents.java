package net.minestom.server.item.component;

import net.kyori.adventure.nbt.*;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
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
        @NotNull List<CustomPotionEffect> customEffects,
        @Nullable String customName
) {
    public static final PotionContents EMPTY = new PotionContents(null, null, List.of(), null);

    public static final NetworkBuffer.Type<PotionContents> NETWORK_TYPE = NetworkBufferTemplate.template(
            PotionType.NETWORK_TYPE.optional(), PotionContents::potion,
            Color.NETWORK_TYPE.optional(), PotionContents::customColor,
            CustomPotionEffect.NETWORK_TYPE.list(Short.MAX_VALUE), PotionContents::customEffects,
            NetworkBuffer.STRING.optional(), PotionContents::customName,
            PotionContents::new);

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

            if (value.customName != null) {
                builder.putString("custom_name", value.customName);
            }

            return builder.build();
        }

        @Override
        public @NotNull PotionContents read(@NotNull BinaryTag tag) {
            // Can be a string with just a potion effect id
            if (tag instanceof StringBinaryTag string) {
                return new PotionContents(PotionType.fromNamespaceId(string.value()), null, List.of(), null);
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

            String customName = null;
            if (compound.get("custom_name") instanceof StringBinaryTag customNameTag)
                customName = customNameTag.value();

            return new PotionContents(potion, customColor, customEffects, customName);
        }
    };

    public PotionContents {
        customEffects = List.copyOf(customEffects);
    }

    public PotionContents(@NotNull PotionType potion) {
        this(potion, null, List.of(), null);
    }

    public PotionContents(@NotNull PotionType potion, @NotNull RGBLike customColor) {
        this(potion, customColor, List.of(), null);
    }

    public PotionContents(@NotNull List<CustomPotionEffect> customEffects) {
        this(null, null, customEffects, null);
    }

    public PotionContents(@NotNull CustomPotionEffect customEffect) {
        this(null, null, List.of(customEffect), null);
    }

    public PotionContents(@Nullable PotionType potion, @Nullable RGBLike customColor, @NotNull List<CustomPotionEffect> customEffects) {
        this(potion, customColor, customEffects, null);
    }

}
