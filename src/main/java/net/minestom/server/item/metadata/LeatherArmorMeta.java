package net.minestom.server.item.metadata;

import net.minestom.server.color.Color;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public record LeatherArmorMeta(TagReadable readable) implements ItemMetaView<LeatherArmorMeta.Builder> {
    private static final Tag<Color> COLOR = Tag.Integer("color").path("display").map(Color::new, Color::asRGB);

    public @Nullable Color getColor() {
        return getTag(COLOR);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder() {
            this(TagHandler.newHandler());
        }

        public Builder color(@Nullable Color color) {
            setTag(COLOR, color);
            return this;
        }
    }
}
