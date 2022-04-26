package net.minestom.server.item.metadata;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public record WritableBookMeta(TagReadable readable) implements ItemMetaView<WritableBookMeta.Builder> {
    private static final Tag<List<Component>> PAGES = Tag.String("pages")
            .<Component>map(s -> LegacyComponentSerializer.legacySection().deserialize(s),
                    textComponent -> LegacyComponentSerializer.legacySection().serialize(textComponent))
            .list().defaultValue(List.of());

    public @NotNull List<@NotNull Component> getPages() {
        return getTag(PAGES);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder() {
            this(TagHandler.newHandler());
        }

        public Builder pages(@NotNull List<@NotNull Component> pages) {
            setTag(PAGES, pages);
            return this;
        }
    }
}
