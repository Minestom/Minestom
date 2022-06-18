package net.minestom.server.message.registry;

import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import static net.minestom.server.message.registry.NBTCompoundWriteable.writeIfPresent;

/**
 * Used to display text either in chat or actionbar via {@link ChatType}
 *
 * @param decoration defines how this text will appear, if null only the message will show
 */
public record TextDisplay(@Nullable ChatDecoration decoration) implements NBTCompoundWriteable {

    public static TextDisplay undecorated() {
        return new TextDisplay(null);
    }

    @Override
    public void write(MutableNBTCompound compound) {
        writeIfPresent("decoration", decoration, compound);
    }
}
