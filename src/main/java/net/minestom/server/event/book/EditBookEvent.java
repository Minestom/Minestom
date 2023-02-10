package net.minestom.server.event.book;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EditBookEvent implements PlayerInstanceEvent, ItemEvent {

    private final Player player;
    private final ItemStack itemStack;
    private final List<String> pages;
    private final String title;

    public EditBookEvent(
            @NotNull Player player,
            @NotNull ItemStack itemStack,
            @NotNull List<String> pages,
            @Nullable String title
    ) {
        this.player = player;
        this.itemStack = itemStack;
        this.pages = pages;
        this.title = title;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    public @NotNull List<String> getPages() {
        return pages;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public boolean isSigned() {
        return title != null;
    }
}
