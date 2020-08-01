package net.minestom.server.item.metadata;

import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTString;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.ArrayList;

public class WritableBookMeta implements ItemMeta {

    private ArrayList<String> pages = new ArrayList<>();

    /**
     * Get an array list containing the book pages
     * <p>
     * The list is modifiable
     *
     * @return a modifiable {@link ArrayList} containing the book pages
     */
    public ArrayList<String> getPages() {
        return pages;
    }

    /**
     * Set the pages list of this book
     *
     * @param pages the pages list
     */
    public void setPages(ArrayList<String> pages) {
        this.pages = pages;
    }

    @Override
    public boolean hasNbt() {
        return !pages.isEmpty();
    }

    @Override
    public boolean isSimilar(ItemMeta itemMeta) {
        if (!(itemMeta instanceof WritableBookMeta))
            return false;
        final WritableBookMeta writableBookMeta = (WritableBookMeta) itemMeta;
        return writableBookMeta.pages.equals(pages);
    }

    @Override
    public void read(NBTCompound compound) {
        if (compound.containsKey("pages")) {
            final NBTList<NBTString> list = compound.getList("pages");
            for (NBTString page : list) {
                this.pages.add(page.getValue());
            }
        }
    }

    @Override
    public void write(NBTCompound compound) {
        if (!pages.isEmpty()) {
            NBTList<NBTString> list = new NBTList<>(NBTTypes.TAG_String);
            for (String page : pages) {
                list.add(new NBTString(page));
            }
            compound.set("pages", list);
        }
    }

    @Override
    public ItemMeta clone() {
        WritableBookMeta writableBookMeta = new WritableBookMeta();
        writableBookMeta.pages.addAll(pages);

        return writableBookMeta;
    }
}
