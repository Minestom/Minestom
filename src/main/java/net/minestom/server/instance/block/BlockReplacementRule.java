package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BlockReplacementRule {
    boolean canReplace(@NotNull BlockFace blockFace,
                       @NotNull Point cursorPosition,
                       @NotNull Material useMaterial);
}