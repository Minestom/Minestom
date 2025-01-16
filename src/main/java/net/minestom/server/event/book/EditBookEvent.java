package net.minestom.server.event.book;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public record EditBookEvent(@NotNull Player player, @NotNull ItemStack itemStack, @NotNull List<String> pages,
                            @Nullable String title) implements PlayerInstanceEvent, ItemEvent {

    @Override
    public @NotNull List<String> pages() {
        return Collections.unmodifiableList(pages);
    }

    public boolean signed() {
        return title != null;
    }
}
