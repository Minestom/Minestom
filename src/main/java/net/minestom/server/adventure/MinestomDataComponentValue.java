package net.minestom.server.adventure;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.event.DataComponentValue;
import net.minestom.server.component.DataComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public record MinestomDataComponentValue(@NotNull DataComponent<?> component, @NotNull Object value) implements DataComponentValue {

}
