package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public record TooltipDisplay(boolean hideTooltip, Set<DataComponent<?>> hiddenComponents) {
    public static final TooltipDisplay EMPTY = new TooltipDisplay(false, Set.of());

    public static final NetworkBuffer.Type<TooltipDisplay> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.BOOLEAN, TooltipDisplay::hideTooltip,
            DataComponent.NETWORK_TYPE.set(Short.MAX_VALUE), TooltipDisplay::hiddenComponents,
            TooltipDisplay::new);
    public static final Codec<TooltipDisplay> CODEC = StructCodec.struct(
            "hide_tooltip", Codec.BOOLEAN, TooltipDisplay::hideTooltip,
            "hidden_components", DataComponent.CODEC.set(Short.MAX_VALUE), TooltipDisplay::hiddenComponents,
            TooltipDisplay::new);

    public @NotNull TooltipDisplay withHideTooltip(boolean hide) {
        return new TooltipDisplay(hide, hiddenComponents);
    }

    public @NotNull TooltipDisplay with(@NotNull DataComponent<?> component) {
        if (!hiddenComponents.contains(component))
            return new TooltipDisplay(hideTooltip, hiddenComponents);

        var newHiddenComponents = new HashSet<>(hiddenComponents);
        newHiddenComponents.remove(component);
        return new TooltipDisplay(hideTooltip, newHiddenComponents);
    }

    public @NotNull TooltipDisplay without(@NotNull DataComponent<?> component) {
        if (hiddenComponents.contains(component))
            return new TooltipDisplay(hideTooltip, hiddenComponents);

        var newHiddenComponents = new HashSet<>(hiddenComponents);
        newHiddenComponents.add(component);
        return new TooltipDisplay(hideTooltip, newHiddenComponents);
    }
}
