package net.minestom.server.registry.dynamic.chat;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.dynamic.DynamicRegistryElement;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import static net.minestom.server.registry.dynamic.chat.NBTCompoundWriteable.writeIfPresent;

public interface ChatType extends DynamicRegistryElement {
    ChatType CHAT = new DynamicChatTypeImpl(Key.key("minecraft:chat"));
    ChatType SYSTEM = new DynamicChatTypeImpl(Key.key("minecraft:system"));

    TextDisplay chat();
    TextDisplay overlay();
    Narration narration();

    @Override
    default Key registry() {
        return CHAT_TYPE_REGISTRY;
    }

    @Override
    default NBTCompound toNBT() {
        final MutableNBTCompound compound = new MutableNBTCompound();
        compound.setString("name", key().asString());
        final MutableNBTCompound element = new MutableNBTCompound();
        writeIfPresent("chat", chat(), element);
        writeIfPresent("overlay", overlay(), element);
        writeIfPresent("narration", narration(), element);
        compound.set("element", element.toCompound());
        return compound.toCompound();
    }

    static ChatType fromNBT(NBTCompound compound) {
        final NBTCompound element = compound.getCompound("element");
        return new ChatTypeImpl(compound.getInt("id"), Key.key(compound.getString("name")),
                TextDisplay.fromNBT(element.getCompound("chat")), TextDisplay.fromNBT(element.getCompound("overlay")),
                Narration.fromNBT(element.getCompound("narration")));
    }
}
