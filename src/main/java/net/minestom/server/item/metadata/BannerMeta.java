package net.minestom.server.item.metadata;

import net.kyori.adventure.text.Component;
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

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
public record BannerMeta(TagReadable readable) implements ItemMetaView<BannerMeta.Builder> {

    private static final Tag<Component> CUSTOM_NAME = Tag.Component("CustomName")
            .path("BlockEntityTag");
    private static final Tag<List<BannerPattern>> PATTERNS = Tag.Structure("Patterns",
            TagSerializer.fromCompound(BannerPattern::fromCompound, BannerPattern::asCompound))
            .path("BlockEntityTag").list().defaultValue(List.of());

    public @Nullable Component getCustomName() {
        return getTag(CUSTOM_NAME);
    }

    public @NotNull List<BannerPattern> getPatterns() {
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

        public Builder customName(Component customName) {
            setTag(CUSTOM_NAME, customName);
            return this;
        }

        public Builder patterns(List<BannerPattern> patterns) {
            setTag(PATTERNS, patterns);
            return this;
        }

        public Builder addPattern(BannerPattern pattern) {
            var newList = new ArrayList<>(getTag(PATTERNS));
            newList.add(pattern);
            return patterns(newList);
        }

        public Builder removePattern(BannerPattern pattern) {
            var newList = new ArrayList<>(getTag(PATTERNS));
            newList.remove(pattern);
            return patterns(newList);
        }
    }

}
