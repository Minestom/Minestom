package net.minestom.server.message.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import static net.minestom.server.message.registry.NBTCompoundWriteable.writeIfPresent;

public final class ChatTypeBuilder {
    private final Key key;
    @Nullable
    private TextDisplay chat;
    @Nullable
    private TextDisplay overlay;
    @Nullable
    private Narration narration;

    public ChatTypeBuilder(Key key) {
        this.key = key;
    }

    @Contract("_ -> new")
    public static ChatTypeBuilder builder(Key key) {
        return new ChatTypeBuilder(key);
    }

    @Contract("_ -> this")
    public ChatTypeBuilder chat(TextDisplay display) {
        this.chat = display;
        return this;
    }

    @Contract("_ -> this")
    public ChatTypeBuilder chat(ChatDecoration decoration) {
        return chat(new TextDisplay(decoration));
    }

    @Contract(" -> this")
    public ChatTypeBuilder chat() {
        return chat(TextDisplay.undecorated());
    }

    @Contract("_ -> this")
    public ChatTypeBuilder overlay(TextDisplay display) {
        this.overlay = display;
        return this;
    }

    @Contract("_ -> this")
    public ChatTypeBuilder overlay(ChatDecoration decoration) {
        return overlay(new TextDisplay(decoration));
    }

    @Contract(" -> this")
    public ChatTypeBuilder overlay() {
        return overlay(TextDisplay.undecorated());
    }

    @Contract("_ -> this")
    public ChatTypeBuilder narration(Narration narration) {
        this.narration = narration;
        return this;
    }

    @Contract("_ -> this")
    public ChatTypeBuilder narration(Narration.Priority priority) {
        return narration(new Narration(null, priority));
    }

    @Contract("_, _ -> this")
    public ChatTypeBuilder narration(ChatDecoration decoration, Narration.Priority priority) {
        return narration(new Narration(decoration, priority));
    }

    @Contract(" -> new")
    public NBTCompound build() {
        final MutableNBTCompound compound = new MutableNBTCompound();
        compound.setString("name", key.asString());
        final MutableNBTCompound element = new MutableNBTCompound();
        writeIfPresent("chat", chat, element);
        writeIfPresent("overlay", overlay, element);
        writeIfPresent("narration", narration, element);
        compound.set("element", element.toCompound());
        return compound.toCompound();
    }
}
