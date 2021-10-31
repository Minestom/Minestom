package net.minestom.server.item.metadata;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.adventure.Localizable;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.*;
import java.util.function.Supplier;

public class WrittenBookMeta extends ItemMeta implements ItemMetaBuilder.Provider<WrittenBookMeta.Builder> {

    private final boolean resolved;
    private final WrittenBookGeneration generation;
    private final String author;
    private final String title;
    private final List<Component> pages;

    protected WrittenBookMeta(@NotNull ItemMetaBuilder metaBuilder, boolean resolved,
                              @Nullable WrittenBookGeneration generation,
                              @Nullable String author, @Nullable String title,
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

    public @Nullable String getAuthor() {
        return author;
    }

    public @Nullable String getTitle() {
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
     * @param book        the book
     * @param localizable who the book is for
     * @return the meta
     */
    public static @NotNull WrittenBookMeta fromAdventure(@NotNull Book book, @NotNull Localizable localizable) {
        return new Builder()
                .resolved(false)
                .generation(WrittenBookGeneration.ORIGINAL)
                .author(GsonComponentSerializer.gson().serialize(GlobalTranslator.render(book.author(), Objects.requireNonNullElse(localizable.getLocale(), MinestomAdventure.getDefaultLocale()))))
                .title(GsonComponentSerializer.gson().serialize(GlobalTranslator.render(book.title(), Objects.requireNonNullElse(localizable.getLocale(), MinestomAdventure.getDefaultLocale()))))
                .pages(book.pages())
                .build();
    }

    public static class Builder extends ItemMetaBuilder {

        private boolean resolved;
        private WrittenBookGeneration generation;
        private String author;
        private String title;
        private List<Component> pages = new ArrayList<>();

        public Builder resolved(boolean resolved) {
            this.resolved = resolved;
            mutateNbt(compound -> compound.setByte("resolved", (byte) (resolved ? 1 : 0)));
            return this;
        }

        public Builder generation(@Nullable WrittenBookGeneration generation) {
            this.generation = generation;
            handleNullable(generation, "generation",
                    () -> new NBTInt(Objects.requireNonNull(generation).ordinal()));
            return this;
        }

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

            handleCollection(pages, "pages", () -> {
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
                author(nbtCompound.getString("author"));
            }
            if (nbtCompound.containsKey("title")) {
                title(nbtCompound.getString("title"));
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
