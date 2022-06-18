package net.minestom.server.message.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import static net.minestom.server.message.registry.NBTCompoundWriteable.writeIfPresent;

/**
 * Describes a chat type
 *
 * @param name name of this type
 * @param chat if present the message sent with this type will show in chat
 * @param overlay if present the message will show in the action bar
 * @param narration id present the message can be narrated by the client if it's enabled clientside
 */
public record ChatType(Key name, @Nullable TextDisplay chat, @Nullable TextDisplay overlay, @Nullable Narration narration) implements NBTCompoundWriteable {

    public static ChatType chat(Key name, TextDisplay display) {
        return chat(name, display, null);
    }

    public static ChatType chat(Key name, TextDisplay display, Narration narration) {
        return new ChatType(name, display, null, narration);
    }

    public static ChatType actionbar(Key name, TextDisplay display) {
        return actionbar(name, display, null);
    }

    public static ChatType actionbar(Key name, TextDisplay display, Narration narration) {
        return new ChatType(name, null, display, narration);
    }

    public static ChatType narration(Key name, Narration narration) {
        return new ChatType(name, null, null, narration);
    }

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
