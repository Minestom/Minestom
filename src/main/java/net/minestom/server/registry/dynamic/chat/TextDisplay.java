package net.minestom.server.registry.dynamic.chat;

import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import static net.minestom.server.registry.dynamic.chat.NBTCompoundWriteable.writeIfPresent;

/**
 * Used to display text either in chat or actionbar via {@link ChatTypeBuilder}
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

    public static TextDisplay fromNBT(NBTCompound compound) {
        if (compound == null) return null;
        final NBTCompound d = compound.getCompound("decoration");
        if (d == null) return undecorated();
        return new TextDisplay(ChatDecoration.fromNBT(d));
    }
}
