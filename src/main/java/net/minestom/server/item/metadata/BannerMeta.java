package net.minestom.server.item.metadata;

import net.kyori.adventure.text.Component;
import net.minestom.server.color.DyeColor;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.banner.BannerPattern;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Meta for all {@link net.minestom.server.item.Material#WHITE_BANNER} and {@link net.minestom.server.item.Material#SHIELD} items
 *
 * @param readable data
 */
@ApiStatus.Experimental
public record BannerMeta(TagReadable readable) implements ItemMetaView<BannerMeta.Builder> {

    private static final Tag<Component> CUSTOM_NAME = Tag.Component("CustomName")
            .path("BlockEntityTag");
    private static final Tag<List<Pattern>> PATTERNS = Tag.Structure("Patterns",
            TagSerializer.fromCompound(Pattern::fromCompound, Pattern::asCompound))
            .path("BlockEntityTag").list().defaultValue(List.of());

    /**
     * Get name of the marker, unused by Minestom
     *
     * @return name of the marker
     */
    public @Nullable Component getCustomName() {
        return getTag(CUSTOM_NAME);
    }

    /**
     * Get patterns of the banner
     *
     * @return patterns of the banner
     */
    public @NotNull List<Pattern> getPatterns() {
        return getTag(PATTERNS);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder() {
            this(TagHandler.newHandler());
        }

        /**
         * Set name of the marker, unused by Minestom
         *
         * @param customName name of the marker
         * @return this
         */
        public Builder customName(Component customName) {
            setTag(CUSTOM_NAME, customName);
            return this;
        }

        /**
         * Set the patterns of the banner
         *
         * @param patterns patterns of the banner
         * @return this
         */
        public Builder patterns(List<Pattern> patterns) {
            setTag(PATTERNS, patterns);
            return this;
        }

        /**
         * Add a pattern to the banner
         *
         * @param pattern pattern to add
         * @return this
         */
        public Builder addPattern(Pattern pattern) {
            var newList = new ArrayList<>(getTag(PATTERNS));
            newList.add(pattern);
            return patterns(newList);
        }

        /**
         * Remove a pattern from the banner
         *
         * @param pattern pattern to remove
         * @return this
         */
        public Builder removePattern(Pattern pattern) {
            var newList = new ArrayList<>(getTag(PATTERNS));
            newList.remove(pattern);
            return patterns(newList);
        }
    }

    public record Pattern(@NotNull DyeColor color, @NotNull BannerPattern pattern) {

        /**
         * Retrieves a banner pattern from the given {@code compound}.
         *
         * @param compound The NBT connection, which should be a banner pattern.
         * @return A new created banner pattern.
         */
        public static @NotNull Pattern fromCompound(@NotNull NBTCompound compound) {
            DyeColor color = compound.containsKey("Color") ? DyeColor.values()[compound.getByte("Color")] : DyeColor.WHITE;
            BannerPattern type;
            if (compound.containsKey("Pattern")) {
                BannerPattern pattern = BannerPattern.fromIdentifier(compound.getString("Pattern"));
                type = pattern != null ? pattern : BannerPattern.BASE;
            } else type = BannerPattern.BASE;
            return new Pattern(color, type);
        }

        /**
         * Retrieves the {@link Pattern} as an {@link NBTCompound}.
         *
         * @return The banner pattern as a nbt compound.
         */
        public @NotNull NBTCompound asCompound() {
            return NBT.Compound(Map.of(
                    "Color", NBT.Byte(color.ordinal()),
                    "Pattern", NBT.String(pattern.identifier())
            ));
        }

    }

}
