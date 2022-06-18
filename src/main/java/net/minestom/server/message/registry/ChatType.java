package net.minestom.server.message.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import static net.minestom.server.message.registry.NBTCompoundWriteable.writeIfPresent;

public record ChatType(Key name, @Nullable TextDisplay chat, @Nullable TextDisplay overlay, @Nullable Narration narration) implements NBTCompoundWriteable {

    @Override
    public void write(MutableNBTCompound compound) {
        compound.setString("name", name.asString());
        final MutableNBTCompound element = new MutableNBTCompound();
        writeIfPresent("chat", chat, element);
        writeIfPresent("overlay", overlay, element);
        writeIfPresent("narration", narration, element);
        compound.set("element", element.toCompound());
    }

}
