package net.minestom.server.item.metadata;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTString;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.ArrayList;
import java.util.List;

public class WritableBookMeta extends ItemMeta {

    private List<String> pages = new ArrayList<>();

    /**
     * Gets an array list containing the book pages.
     * <p>
     * The list is modifiable.
     *
     * @return a modifiable {@link ArrayList} containing the book pages
     */
    @NotNull
    public List<String> getPages() {
        return pages;
    }

    /**
     * Sets the pages list of this book.
     *
     * @param pages the pages list
     */
    public void setPages(@NotNull List<String> pages) {
        this.pages = pages;
    }

    @Override
    public boolean hasNbt() {
        return !pages.isEmpty();
    }

    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        if (!(itemMeta instanceof WritableBookMeta))
            return false;
        final WritableBookMeta writableBookMeta = (WritableBookMeta) itemMeta;
        return writableBookMeta.pages.equals(pages);
    }

    @Override
    public void read(@NotNull NBTCompound compound) {
        if (compound.containsKey("pages")) {
            final NBTList<NBTString> list = compound.getList("pages");
            for (NBTString page : list) {
                this.pages.add(page.getValue());
            }
        }
    }

    @Override
    public void write(@NotNull NBTCompound compound) {
        if (!pages.isEmpty()) {
            NBTList<NBTString> list = new NBTList<>(NBTTypes.TAG_String);
            for (String page : pages) {
                list.add(new NBTString(page));
            }
            compound.set("pages", list);
        }
    }

    @NotNull
    @Override
    public ItemMeta clone() {
        WritableBookMeta writableBookMeta = (WritableBookMeta) super.clone();
        writableBookMeta.pages = new ArrayList<>(pages);
        return writableBookMeta;
    }
}
