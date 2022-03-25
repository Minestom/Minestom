package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SidebarBuilder {

    public Component title;
    public final List<Component> lines = new ArrayList<>();

    /*package-private*/ SidebarBuilder(@NotNull Component title) {
        this.title = title;
    }

    public SidebarUI build() {
        return new SidebarUIImpl(title, lines);
    }

    public SidebarBuilder title(@NotNull Component title) {
        this.title = title;
        return this;
    }

    public SidebarBuilder add(@NotNull Component line) {
        this.lines.add(line);
        return this;
    }

    public SidebarBuilder set(@Range(from = 0, to = 15) int index, @NotNull Component line) {
        while (lines.size() <= index) {
            lines.add(Component.empty());
        }

        lines.set(index, line);

        return this;
    }

}
