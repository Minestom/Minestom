package net.minestom.server.item.meta;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.adventure.AdventureSerializer;
import net.minestom.server.adventure.Localizable;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTString;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class WrittenBookMeta extends ItemMeta implements ItemMetaBuilder.Provider<WrittenBookMeta.Builder> {

    private final boolean resolved;
    private final WrittenBookGeneration generation;
    private final String author;
    private final String title;
    private final List<Component> pages;

    protected WrittenBookMeta(@NotNull ItemMetaBuilder metaBuilder, boolean resolved,
                              WrittenBookGeneration generation, String author, String title,
                              List<Component> pages) {
        super(metaBuilder);
        this.resolved = resolved;
        this.generation = generation;
        this.author = author;
        this.title = title;
        this.pages = Collections.unmodifiableList(pages);
    }

    public boolean isResolved() {
        return resolved;
    }

    public WrittenBookGeneration getGeneration() {
        return generation;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public List<Component> getPages() {
        return pages;
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
                .author(AdventureSerializer.translateAndSerialize(book.author(), localizable))
                .title(AdventureSerializer.translateAndSerialize(book.title(), localizable))
                .pages(book.pages())
                .build();
    }

    public static class Builder extends ItemMetaBuilder {

        private boolean resolved;
        private WrittenBookGeneration generation;
        private String author;
        private String title;
        private List<Component> pages;

        public Builder resolved(boolean resolved) {
            this.resolved = resolved;
            this.nbt.setByte("resolved", (byte) (resolved ? 1 : 0));
            return this;
        }

        public Builder generation(WrittenBookGeneration generation) {
            this.generation = generation;
            this.nbt.setInt("generation", generation.ordinal());
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            this.nbt.setString("author", author);
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            this.nbt.setString("title", author);
            return this;
        }

        public Builder pages(List<Component> pages) {
            this.pages = pages;
            this.nbt.setString("title", author);

            NBTList<NBTString> list = new NBTList<>(NBTTypes.TAG_String);
            for (Component page : pages) {
                list.add(new NBTString(AdventureSerializer.serialize(page)));
            }
            this.nbt.set("pages", list);

            return this;
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
