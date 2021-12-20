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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        this.pages = List.copyOf(pages);
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
            mutableNbt().set("resolved", NBT.Boolean(resolved));
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

            handleCollection(pages, "pages", () -> NBT.List(
                    NBTType.TAG_String,
                    pages.stream()
                            .map(page -> new NBTString(GsonComponentSerializer.gson().serialize(page)))
                            .toList()
            ));

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
            if (nbtCompound.get("resolved") instanceof NBTByte resolved) {
                this.resolved = resolved.asBoolean();
            }
            if (nbtCompound.get("generation") instanceof NBTInt generation) {
                this.generation = WrittenBookGeneration.values()[generation.getValue()];
            }
            if (nbtCompound.get("author") instanceof NBTString author) {
                this.author = author.getValue();
            }
            if (nbtCompound.get("title") instanceof NBTString title) {
                this.title = title.getValue();
            }
            if (nbtCompound.get("pages") instanceof NBTList<?> list &&
                    list.getSubtagType() == NBTType.TAG_String) {
                for (NBTString page : list.<NBTString>asListOf()) {
                    this.pages.add(GsonComponentSerializer.gson().deserialize(page.getValue()));
                }
            }
        }
    }
}
