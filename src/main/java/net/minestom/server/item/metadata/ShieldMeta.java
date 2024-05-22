package net.minestom.server.item.metadata;

import net.kyori.adventure.text.Component;
import net.minestom.server.color.DyeColor;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

// Microtus -  Banner and Shield Meta
@ApiStatus.Experimental
public record ShieldMeta(@NotNull TagReadable readable) implements ItemMetaView<ShieldMeta.Builder> {

    private static final String BASE_KEY = "Base";
    private static final String ENTITY_TAG = "BlockEntityTag";
    private static final Tag<Component> CUSTOM_NAME = Tag.Component("CustomName").path(ENTITY_TAG);
    private static final Tag<Integer> BASE_TAG = Tag.Integer(BASE_KEY).path(ENTITY_TAG);
    private static final Tag<List<BannerMeta.Pattern>> PATTERNS = Tag.Structure("Patterns",
                    TagSerializer.fromCompound(BannerMeta.Pattern::fromCompound, BannerMeta.Pattern::asCompound))
            .path(ENTITY_TAG).list().defaultValue(List.of());

    /**
     * Get base color of the shield
     *
     * @return the base color
     */
    public @NotNull DyeColor getBaseColor() {
        return DyeColor.getValue(getTag(BASE_TAG));
    }

    /**
     * Get name of the marker, unused by Minestom
     *
     * @return name of the marker
     */
    public @Nullable Component getCustomName() {
        return getTag(CUSTOM_NAME);
    }

    /**
     * Get patterns of the shield
     *
     * @return patterns of the shield
     */
    public @NotNull List<BannerMeta.Pattern> getPatterns() {
        return getTag(PATTERNS);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(@NotNull TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder() {
            this(TagHandler.newHandler());
        }

        /**
         * Set the color which is used as base color for a shield.
         * @param dyeColor the color to set
         * @return the builder instance
         */
        public @NotNull ShieldMeta.Builder baseColor(@NotNull DyeColor dyeColor) {
            setTag(BASE_TAG, dyeColor.ordinal());
            return this;
        }

        /**
         * Set name of the marker, unused by Minestom
         *
         * @param customName name of the marker
         * @return this
         */
        public @NotNull ShieldMeta.Builder customName(@NotNull Component customName) {
            setTag(CUSTOM_NAME, customName);
            return this;
        }

        /**
         * Set the patterns of the shield
         *
         * @param patterns patterns of the shield
         * @return this
         */
        public @NotNull ShieldMeta.Builder patterns(@NotNull List<BannerMeta.Pattern> patterns) {
            setTag(PATTERNS, patterns);
            return this;
        }

        /**
         * Add a pattern to the shield
         *
         * @param pattern pattern to add
         * @return this
         */
        public @NotNull ShieldMeta.Builder addPattern(BannerMeta.Pattern pattern) {
            var newList = new ArrayList<>(getTag(PATTERNS));
            newList.add(pattern);
            return patterns(newList);
        }

        /**
         * Remove a pattern from the shield
         *
         * @param pattern pattern to remove
         * @return this
         */
        public @NotNull ShieldMeta.Builder removePattern(BannerMeta.Pattern pattern) {
            var newList = new ArrayList<>(getTag(PATTERNS));
            newList.remove(pattern);
            return patterns(newList);
        }

        /**
         * Clears the underlying list which contains the pattern values.
         * @return this
         */
        public @NotNull ShieldMeta.Builder clearPatterns() {
            return patterns(List.of());
        }
    }
}
