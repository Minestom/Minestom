package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public sealed interface TabListUI permits TabListUIImpl {
    static Builder builder() {
        return new TabListUIImpl.Builder();
    }

    interface Builder {
        @NotNull Builder header(@NotNull Component header);

        @NotNull Builder footer(@NotNull Component footer);

        @NotNull Builder addBefore(@NotNull Component text, @Nullable PlayerSkin skin);

        @NotNull Builder addAfter(@NotNull Component text, @Nullable PlayerSkin skin);

        @NotNull Builder setBefore(@Range(from = 0, to = 80) int index, @NotNull Component text, @Nullable PlayerSkin skin);

        @NotNull Builder setAfter(@Range(from = 0, to = 80) int index, @NotNull Component text, @Nullable PlayerSkin skin);

        @NotNull TabListUI build();
    }
}
