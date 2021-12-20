package net.minestom.server.item.metadata;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        this.pages = List.copyOf(pages);
    }

    public @Nullable String getAuthor() {
        return author;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public @NotNull List<@NotNull Component> getPages() {
        return pages;
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
            handleNullable(title, "title", () -> NBT.String(Objects.requireNonNull(title)));
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
            if (nbtCompound.get("author") instanceof NBTString author) {
                this.author = author.getValue();
            }
            if (nbtCompound.get("title") instanceof NBTString title) {
                this.title = title.getValue();
            }
            if (nbtCompound.get("pages") instanceof NBTList<?> list &&
                    list.getSubtagType() == NBTType.TAG_String) {
                for (NBTString page : list.<NBTString>asListOf()) {
                    this.pages.add(LegacyComponentSerializer.legacySection().deserialize(page.getValue()));
                }
            }
        }
    }
}
