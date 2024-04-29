package net.minestom.server.item.component;

import net.kyori.adventure.nbt.*;
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
        @Nullable Color customColor,
        @NotNull List<CustomPotionEffect> customEffects
) {
    public static final int POTION_DRINK_TIME = 32; // 32 ticks, in ms
    public static final PotionContents EMPTY = new PotionContents(null, null, List.of());

    public static final NetworkBuffer.Type<PotionContents> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, PotionContents value) {
            Integer typeId = value.potion == null ? null : value.potion.id();
            buffer.writeOptional(NetworkBuffer.VAR_INT, typeId);
            buffer.writeOptional(NetworkBuffer.COLOR, value.customColor);
            buffer.writeCollection(CustomPotionEffect.NETWORK_TYPE, value.customEffects);
        }

        @Override
        public PotionContents read(@NotNull NetworkBuffer buffer) {
            Integer typeId = buffer.readOptional(NetworkBuffer.VAR_INT);
            return new PotionContents(
                    typeId == null ? null : PotionType.fromId(typeId),
                    buffer.readOptional(NetworkBuffer.COLOR),
                    buffer.readCollection(CustomPotionEffect.NETWORK_TYPE, Short.MAX_VALUE)
            );
        }
    };

    public static final BinaryTagSerializer<PotionContents> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull PotionContents value) {
            throw new UnsupportedOperationException();
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
}
