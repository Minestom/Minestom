package net.minestom.server.item.meta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.adventure.AdventureSerializer;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTString;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class WritableBookMeta extends ItemMeta implements ItemMetaBuilder.Provider<WritableBookMeta.Builder> {

    private final String author;
    private final String title;
    private final List<Component> pages;

    protected WritableBookMeta(@NotNull ItemMetaBuilder metaBuilder,
                               String author, String title,
                               List<Component> pages) {
        super(metaBuilder);
        this.author = author;
        this.title = title;
        this.pages = new ArrayList<>(pages);
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public @NotNull List<@NotNull Component> getPages() {
        return Collections.unmodifiableList(pages);
    }

    public static class Builder extends ItemMetaBuilder {

        private String author;
        private String title;
        private List<Component> pages = new ArrayList<>();

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
                    this.pages.add(GsonComponentSerializer.gson().deserialize(page.getValue()));
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