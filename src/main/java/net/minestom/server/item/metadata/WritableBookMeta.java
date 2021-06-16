package net.minestom.server.item.metadata;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.adventure.AdventureSerializer;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTString;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class WritableBookMeta extends ItemMeta implements ItemMetaBuilder.Provider<WritableBookMeta.Builder> {

    private final List<Component> pages;

    protected WritableBookMeta(@NotNull ItemMetaBuilder metaBuilder, @NotNull List<@NotNull Component> pages) {
        super(metaBuilder);
        this.pages = new ArrayList<>(pages);
    }

    public @NotNull List<@NotNull Component> getPages() {
        return Collections.unmodifiableList(pages);
    }

    public static class Builder extends ItemMetaBuilder {

        private String author;
        private String title;
        private List<Component> pages = new ArrayList<>();

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

        @Override
        public @NotNull WritableBookMeta build() {
            return new WritableBookMeta(this, pages);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            final NBTList<NBTString> list = nbtCompound.getList("pages");
            if (list != null) {
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