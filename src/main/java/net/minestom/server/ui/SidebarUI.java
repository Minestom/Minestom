package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;

public sealed interface SidebarUI permits SidebarUIImpl {
    static @NotNull SidebarUI of(@NotNull Component title, @NotNull List<@NotNull Component> lines) {
        return new SidebarUIImpl(title, lines);
    }

    static @NotNull Builder builder(@NotNull Component title) {
        return new SidebarUIImpl.Builder(title);
    }

    interface Builder {
        @NotNull Builder title(@NotNull Component title);

        @NotNull Builder add(@NotNull Component line);

        @NotNull Builder set(@Range(from = 0, to = 15) int index, @NotNull Component line);

        @NotNull SidebarUI build();
    }
}
