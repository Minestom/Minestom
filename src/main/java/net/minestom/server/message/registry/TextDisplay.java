package net.minestom.server.message.registry;

import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import static net.minestom.server.message.registry.NBTCompoundWriteable.writeIfPresent;

public record TextDisplay(@Nullable ChatDecoration decoration) implements NBTCompoundWriteable {

    @Override
    public void write(MutableNBTCompound compound) {
        writeIfPresent("decoration", decoration, compound);
    }
}
