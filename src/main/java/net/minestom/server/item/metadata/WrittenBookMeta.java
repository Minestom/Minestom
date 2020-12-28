package net.minestom.server.item.metadata;

import net.minestom.server.chat.ChatParser;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTString;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.ArrayList;
import java.util.List;

public class WrittenBookMeta extends ItemMeta {

    private boolean resolved;
    private WrittenBookGeneration generation;
    private String author;
    private String title;
    private List<JsonMessage> pages = new ArrayList<>();

    /**
     * Gets if the book is resolved.
     *
     * @return true if the book is resolved, false otherwise
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Sets to true when the book (or a book from the stack)
     * is opened for the first time after being created.
     *
     * @param resolved true to make the book resolved, false otherwise
     */
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    /**
     * Gets the copy tier of the book.
     *
     * @return the copy tier of the book
     */
    public WrittenBookGeneration getGeneration() {
        return generation;
    }

    /**
     * Sets the copy tier of the book.
     *
     * @param generation the copy tier of the book
     */
    public void setGeneration(WrittenBookGeneration generation) {
        this.generation = generation;
    }

    /**
     * Gets the author of the book.
     *
     * @return the author of the book
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of the book.
     *
     * @param author the author of the book
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the title of the book.
     *
     * @return the title of the book
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the book.
     *
     * @param title the title of the book
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets an {@link ArrayList} containing all the pages.
     * <p>
     * The list is modifiable.
     *
     * @return a modifiable {@link ArrayList} with the pages of the book
     */
    public List<JsonMessage> getPages() {
        return pages;
    }

    /**
     * Sets the {@link ArrayList} containing the book pages.
     *
     * @param pages the array list containing the book pages
     */
    public void setPages(List<JsonMessage> pages) {
        this.pages = pages;
    }

    @Override
    public boolean hasNbt() {
        return resolved || generation != null ||
                author != null || title != null ||
                !pages.isEmpty();
    }

    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        if (!(itemMeta instanceof WrittenBookMeta))
            return false;
        final WrittenBookMeta writtenBookMeta = (WrittenBookMeta) itemMeta;
        return writtenBookMeta.resolved == resolved &&
                writtenBookMeta.generation == generation &&
                writtenBookMeta.author.equals(author) &&
                writtenBookMeta.title.equals(title) &&
                writtenBookMeta.pages.equals(pages);
    }

    @Override
    public void read(@NotNull NBTCompound compound) {
        if (compound.containsKey("resolved")) {
            this.resolved = compound.getByte("resolved") == 1;
        }
        if (compound.containsKey("generation")) {
            this.generation = WrittenBookGeneration.values()[compound.getInt("generation")];
        }
        if (compound.containsKey("author")) {
            this.author = compound.getString("author");
        }
        if (compound.containsKey("title")) {
            this.title = compound.getString("title");
        }
        if (compound.containsKey("pages")) {
            final NBTList<NBTString> list = compound.getList("pages");
            for (NBTString page : list) {
                final String jsonPage = page.getValue();
                final ColoredText coloredText = ChatParser.toColoredText(jsonPage);
                this.pages.add(coloredText);
            }
        }
    }

    @Override
    public void write(@NotNull NBTCompound compound) {
        if (resolved) {
            compound.setByte("resolved", (byte) 1);
        }
        if (generation != null) {
            compound.setInt("generation", generation.ordinal());
        }
        if (author != null) {
            compound.setString("author", author);
        }
        if (title != null) {
            compound.setString("title", title);
        }
        if (!pages.isEmpty()) {
            NBTList<NBTString> list = new NBTList<>(NBTTypes.TAG_String);
            for (JsonMessage page : pages) {
                list.add(new NBTString(page.toString()));
            }
            compound.set("pages", list);
        }
    }

    @NotNull
    @Override
    public ItemMeta clone() {
        WrittenBookMeta writtenBookMeta = (WrittenBookMeta) super.clone();
        writtenBookMeta.resolved = resolved;
        writtenBookMeta.generation = generation;
        writtenBookMeta.author = author;
        writtenBookMeta.title = title;
        writtenBookMeta.pages.addAll(pages);

        return writtenBookMeta;
    }

    public enum WrittenBookGeneration {
        ORIGINAL, COPY_OF_ORIGINAL, COPY_OF_COPY, TATTERED
    }

}
