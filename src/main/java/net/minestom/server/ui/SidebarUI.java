package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public sealed interface SidebarUI permits SidebarUIImpl {
    static Builder builder(@NotNull Component title) {
        return new SidebarUIImpl.Builder(title);
    }

    interface Builder {
        @NotNull Builder title(@NotNull Component title);

        @NotNull Builder add(@NotNull Component line);

        @NotNull Builder set(@Range(from = 0, to = 15) int index, @NotNull Component line);

        @NotNull SidebarUI build();
    }
}
