package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public sealed interface SidebarUI permits SidebarUIImpl {

    static SidebarBuilder builder(@NotNull Component title) {
        return new SidebarBuilder(title);
    }

}
