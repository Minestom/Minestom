package net.minestom.server.dialog;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public record DialogActionButton(
        Component label,
        @Nullable Component tooltip,
        int width,
        @Nullable DialogAction action
) implements ComponentHolder<DialogActionButton> {
    public static final int DEFAULT_WIDTH = 150;
    public static final StructCodec<DialogActionButton> CODEC = StructCodec.struct(
            "label", Codec.COMPONENT, DialogActionButton::label,
            "tooltip", Codec.COMPONENT.optional(), DialogActionButton::tooltip,
            "width", Codec.INT.optional(DEFAULT_WIDTH), DialogActionButton::width,
            "action", DialogAction.CODEC.optional(), DialogActionButton::action,
            DialogActionButton::new);

    @Override
    public List<Component> components() {
        List<Component> components = new ArrayList<>();
        components.add(label);
        if (tooltip != null) components.add(tooltip);
        return List.copyOf(components);
    }

    @Override
    public DialogActionButton copyWithOperator(UnaryOperator<Component> operator) {
        return new DialogActionButton(
                operator.apply(label),
                tooltip != null ? operator.apply(tooltip) : null,
                width,
                action
        );
    }
}
