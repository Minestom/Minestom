package net.minestom.server.item.metadata;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WritableBookMeta extends ItemMeta implements ItemMetaBuilder.Provider<WritableBookMeta.Builder> {

    private final String author;
    private final String title;
    private final List<Component> pages;

    protected WritableBookMeta(@NotNull ItemMetaBuilder metaBuilder,
                               @Nullable String author, @Nullable String title,
                               @NotNull List<@NotNull Component> pages) {
        super(metaBuilder);
        this.author = author;
        this.title = title;
        this.pages = new ArrayList<>(pages);
    }

    public @Nullable String getAuthor() {
        return author;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public @NotNull List<@NotNull Component> getPages() {
        return Collections.unmodifiableList(pages);
    }

    public static class Builder extends ItemMetaBuilder {

        private String author;
        private String title;
        private List<Component> pages = new ArrayList<>();

        public Builder author(@Nullable String author) {
            this.author = author;
            handleNullable(author, "author",
                    () -> new NBTString(Objects.requireNonNull(author)));
            return this;
        }

        public Builder title(@Nullable String title) {
            this.title = title;
            handleNullable(title, "title",
                    () -> new NBTString(Objects.requireNonNull(title)));
            return this;
        }

        public Builder pages(@NotNull List<@NotNull Component> pages) {
            this.pages = new ArrayList<>(pages);

            handleCollection(pages, "pages", () -> NBT.List(
                    NBTType.TAG_String,
                    pages.stream()
                            .map(page -> new NBTString(LegacyComponentSerializer.legacySection().serialize(page)))
                            .toList()
            ));

            return this;
        }

        @Override
        public @NotNull WritableBookMeta build() {
            return new WritableBookMeta(this, author, title, pages);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.containsKey("author")) {
                author(nbtCompound.getString("author"));
            }
            if (nbtCompound.containsKey("title")) {
                title(nbtCompound.getString("title"));
            }
            if (nbtCompound.containsKey("pages")) {
                final NBTList<NBTString> list = nbtCompound.getList("pages");
                for (NBTString page : list) {
                    this.pages.add(LegacyComponentSerializer.legacySection().deserialize(page.getValue()));
                }
                pages(pages);
            }
        }

        @Override
        protected @NotNull Supplier<ItemMetaBuilder> getSupplier() {
            return WritableBookMeta.Builder::new;
        }
    }
}
