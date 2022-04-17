package net.minestom.server.item.metadata;

import net.minestom.server.color.Color;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.tag.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public record PotionMeta(TagReadable readable) implements ItemMetaView<PotionMeta.Builder> {
    private static final Tag<PotionType> POTION_TYPE = Tag.String("Potion").map(PotionType::fromNamespaceId, ProtocolObject::name).defaultValue(PotionType.EMPTY);
    private static final Tag<List<CustomPotionEffect>> CUSTOM_POTION_EFFECTS = Tag.Structure("CustomPotionEffects", new TagSerializer<CustomPotionEffect>() {
        @Override
        public @Nullable CustomPotionEffect read(@NotNull TagReadable reader) {
            final Byte id = reader.getTag(Tag.Byte("Id"));
            final Byte amplifier = reader.getTag(Tag.Byte("Amplifier"));
            final Integer duration = reader.getTag(Tag.Integer("Duration"));
            final Boolean ambient = reader.getTag(Tag.Boolean("Ambient"));
            final Boolean showParticles = reader.getTag(Tag.Boolean("ShowParticles"));
            final Boolean showIcon = reader.getTag(Tag.Boolean("ShowIcon"));
            if (id == null || amplifier == null || duration == null || ambient == null || showParticles == null || showIcon == null) {
                return null;
            }
            return new CustomPotionEffect(id, amplifier, duration, ambient, showParticles, showIcon);
        }

        @Override
        public void write(@NotNull TagWritable writer, @NotNull CustomPotionEffect value) {
            writer.setTag(Tag.Byte("Id"), value.id());
            writer.setTag(Tag.Byte("Amplifier"), value.amplifier());
            writer.setTag(Tag.Integer("Duration"), value.duration());
            writer.setTag(Tag.Boolean("Ambient"), value.isAmbient());
            writer.setTag(Tag.Boolean("ShowParticles"), value.showParticles());
            writer.setTag(Tag.Boolean("ShowIcon"), value.showIcon());
        }
    }).list().defaultValue(List.of());
    private static final Tag<Color> CUSTOM_POTION_COLOR = Tag.Integer("CustomPotionColor").path("display").map(Color::new, Color::asRGB);

    public @NotNull PotionType getPotionType() {
        return getTag(POTION_TYPE);
    }

    public @NotNull List<CustomPotionEffect> getCustomPotionEffects() {
        return getTag(CUSTOM_POTION_EFFECTS);
    }

    public @Nullable Color getColor() {
        return getTag(CUSTOM_POTION_COLOR);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder() {
            this(TagHandler.newHandler());
        }

        public Builder potionType(@NotNull PotionType potionType) {
            setTag(POTION_TYPE, potionType);
            return this;
        }

        public Builder effects(@NotNull List<CustomPotionEffect> customPotionEffects) {
            setTag(CUSTOM_POTION_EFFECTS, customPotionEffects);
            return this;
        }

        public Builder color(@NotNull Color color) {
            setTag(CUSTOM_POTION_COLOR, color);
            return this;
        }
    }
}
