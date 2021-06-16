package net.minestom.server.item.metadata;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.adventure.AdventureSerializer;
import net.minestom.server.adventure.Localizable;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.*;
import java.util.function.Supplier;

public class WrittenBookMeta extends ItemMeta implements ItemMetaBuilder.Provider<WrittenBookMeta.Builder> {

    private final boolean resolved;
    private final WrittenBookGeneration generation;
    private final Component author;
    private final Component title;
    private final List<Component> pages;

    protected WrittenBookMeta(@NotNull ItemMetaBuilder metaBuilder, boolean resolved,
                              @Nullable WrittenBookGeneration generation,
                              @Nullable Component author, @Nullable Component title,
                              @NotNull List<@NotNull Component> pages) {
        super(metaBuilder);
        this.resolved = resolved;
        this.generation = generation;
        this.author = author;
        this.title = title;
        this.pages = new ArrayList<>(pages);
    }

    public boolean isResolved() {
        return resolved;
    }

    public @Nullable WrittenBookGeneration getGeneration() {
        return generation;
    }

    public @Nullable Component getAuthor() {
        return author;
    }

    public @Nullable Component getTitle() {
        return title;
    }

    public @NotNull List<@NotNull Component> getPages() {
        return Collections.unmodifiableList(pages);
    }

    public enum WrittenBookGeneration {
        ORIGINAL, COPY_OF_ORIGINAL, COPY_OF_COPY, TATTERED
    }

    /**
     * Creates a written book meta from an Adventure book. This meta will not be
     * resolved and the generation will default to {@link WrittenBookGeneration#ORIGINAL}.
     *
     * @param book the book
     * @return the meta
     */
    public static @NotNull WrittenBookMeta fromAdventure(@NotNull Book book) {
        return new Builder()
                .resolved(false)
                .generation(WrittenBookGeneration.ORIGINAL)
                .author(book.author())
                .title(book.title())
                .pages(book.pages())
                .build();
    }

    public static class Builder extends ItemMetaBuilder {

        private boolean resolved;
        private WrittenBookGeneration generation;
        private Component author;
        private Component title;
        private List<Component> pages = new ArrayList<>();

        public Builder resolved(boolean resolved) {
            this.resolved = resolved;
            this.nbt.setByte("resolved", (byte) (resolved ? 1 : 0));
            return this;
        }

        public Builder generation(@Nullable WrittenBookGeneration generation) {
            this.generation = generation;
            handleNullable(generation, "generation", nbt,
                    () -> new NBTInt(Objects.requireNonNull(generation).ordinal()));
            return this;
        }

        public Builder author(@Nullable Component author) {
            this.author = author;
            handleNullable(author, "author", nbt,
                    () -> new NBTString(LegacyComponentSerializer.legacySection().serialize(author)));
            return this;
        }

        public Builder title(@Nullable Component title) {
            this.title = title;
            handleNullable(title, "title", nbt,
                    () -> new NBTString(LegacyComponentSerializer.legacySection().serialize(title)));
            return this;
        }

        public Builder pages(@NotNull List<@NotNull Component> pages) {
            this.pages = pages;

            handleCollection(pages, "pages", nbt, () -> {
                NBTList<NBTString> list = new NBTList<>(NBTTypes.TAG_String);
                for (Component page : pages) {
                    list.add(new NBTString(GsonComponentSerializer.gson().serialize(page)));
                }
                return list;
            });

            return this;
        }

        public Builder pages(Component... pages) {
            return pages(Arrays.asList(pages));
        }

        @Override
        public @NotNull WrittenBookMeta build() {
            return new WrittenBookMeta(this, resolved, generation, author, title, pages);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.containsKey("resolved")) {
                resolved(nbtCompound.getByte("resolved") == 1);
            }
            if (nbtCompound.containsKey("generation")) {
                generation(WrittenBookGeneration.values()[nbtCompound.getInt("generation")]);
            }
            if (nbtCompound.containsKey("author")) {
                author(LegacyComponentSerializer.legacySection().deserialize(nbtCompound.getString("author")));
            }
            if (nbtCompound.containsKey("title")) {
                title(LegacyComponentSerializer.legacySection().deserialize(nbtCompound.getString("title")));
            }
            if (nbtCompound.containsKey("pages")) {
                final NBTList<NBTString> list = nbtCompound.getList("pages");
                for (NBTString page : list) {
                    this.pages.add(GsonComponentSerializer.gson().deserialize(page.getValue()));
                }
                pages(pages);
            }
        }

        @Override
        protected @NotNull Supplier<ItemMetaBuilder> getSupplier() {
            return Builder::new;
        }
    }
}
