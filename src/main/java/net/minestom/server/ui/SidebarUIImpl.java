package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

record SidebarUIImpl(Component title,
                     List<Component> lines) implements SidebarUI {
    SidebarUIImpl {
        lines = List.copyOf(lines);
    }

    static final class Builder implements SidebarUI.Builder {
        Component title;
        final List<Component> lines = new ArrayList<>();

        Builder(@NotNull Component title) {
            this.title = title;
        }

        public @NotNull Builder title(@NotNull Component title) {
            this.title = title;
            return this;
        }

        public @NotNull Builder add(@NotNull Component line) {
            this.lines.add(line);
            return this;
        }

        public @NotNull Builder set(@Range(from = 0, to = 15) int index, @NotNull Component line) {
            while (lines.size() <= index) {
                lines.add(Component.empty());
            }
            lines.set(index, line);
            return this;
        }

        public @NotNull SidebarUI build() {
            return new SidebarUIImpl(title, lines);
        }
    }
}
