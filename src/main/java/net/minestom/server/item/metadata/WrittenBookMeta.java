package net.minestom.server.item.metadata;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.List;

public record WrittenBookMeta(TagReadable readable) implements ItemMetaView<WrittenBookMeta.Builder> {
    private static final Tag<Boolean> RESOLVED = Tag.Boolean("resolved").defaultValue(false);
    private static final Tag<WrittenBookGeneration> GENERATION = Tag.Integer("resolved").map(integer -> WrittenBookGeneration.values()[integer], Enum::ordinal);
    private static final Tag<String> AUTHOR = Tag.String("author");
    private static final Tag<String> TITLE = Tag.String("title");
    private static final Tag<List<Component>> PAGES = Tag.String("pages")
            .map(GsonComponentSerializer.gson()::deserialize, GsonComponentSerializer.gson()::serialize)
            .list().defaultValue(List.of());

    public boolean isResolved() {
        return getTag(RESOLVED);
    }

    public @Nullable WrittenBookGeneration getGeneration() {
        return getTag(GENERATION);
    }

    public @Nullable String getAuthor() {
        return getTag(AUTHOR);
    }

    public @Nullable String getTitle() {
        return getTag(TITLE);
    }

    public @NotNull List<@NotNull Component> getPages() {
        return getTag(PAGES);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public enum WrittenBookGeneration {
        ORIGINAL, COPY_OF_ORIGINAL, COPY_OF_COPY, TATTERED
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder() {
            this(TagHandler.newHandler());
        }

        public Builder resolved(boolean resolved) {
            setTag(RESOLVED, resolved);
            return this;
        }

        public Builder generation(@Nullable WrittenBookGeneration generation) {
            setTag(GENERATION, generation);
            return this;
        }

        public Builder author(@Nullable String author) {
            setTag(AUTHOR, author);
            return this;
        }

        public Builder author(@Nullable Component author) {
            return author(author != null ? LegacyComponentSerializer.legacySection().serialize(author) : null);
        }

        public Builder title(@Nullable String title) {
            setTag(TITLE, title);
            return this;
        }

        public Builder title(@Nullable Component title) {
            return title(title != null ? LegacyComponentSerializer.legacySection().serialize(title) : null);
        }

        public Builder pages(@NotNull List<@NotNull Component> pages) {
            setTag(PAGES, pages);
            return this;
        }

        public Builder pages(Component... pages) {
            return pages(Arrays.asList(pages));
        }
    }
}
