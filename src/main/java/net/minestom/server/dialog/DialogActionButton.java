package net.minestom.server.dialog;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DialogActionButton(
        @NotNull Component label,
        @Nullable Component tooltip,
        int width,
        @Nullable DialogAction action
) {
    public static final int DEFAULT_WIDTH = 150;
    public static final StructCodec<DialogActionButton> CODEC = StructCodec.struct(
            "label", Codec.COMPONENT, DialogActionButton::label,
            "tooltip", Codec.COMPONENT.optional(), DialogActionButton::tooltip,
            "width", Codec.INT.optional(DEFAULT_WIDTH), DialogActionButton::width,
            "action", DialogAction.CODEC.optional(), DialogActionButton::action,
            DialogActionButton::new);
}
