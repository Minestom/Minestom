package net.minestom.server.registry.dynamic.chat;

import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

public interface NBTCompoundWriteable {
    static void writeIfPresent(String name, @Nullable NBTCompoundWriteable writeable, MutableNBTCompound element) {
        if (writeable != null) {
            final MutableNBTCompound el = new MutableNBTCompound();
            writeable.write(el);
            element.set(name, el.toCompound());
        }
    }

    void write(MutableNBTCompound compound);
}
