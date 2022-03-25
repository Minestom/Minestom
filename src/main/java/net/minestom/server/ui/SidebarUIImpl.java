package net.minestom.server.ui;

import net.kyori.adventure.text.Component;

import java.util.List;

public record SidebarUIImpl(
        Component title,
        List<Component> lines
) implements SidebarUI {

}
