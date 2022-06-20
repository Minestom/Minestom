package net.minestom.server.registry.dynamic.chat;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.dynamic.DynamicRegistryElement;
import net.minestom.server.registry.dynamic.DynamicRegistryElementBuilder;
import net.minestom.server.registry.dynamic.DynamicRegistryElementFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public final class ChatTypeBuilder implements DynamicRegistryElementBuilder<ChatType> {
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

    @Override
    public NBTCompound toNBT() {
        return new ChatTypeImpl(-1, key, chat, overlay, narration).toNBT();
    }

    @Override
    public Key registry() {
        return DynamicRegistryElement.CHAT_TYPE_REGISTRY;
    }

    @Override
    public DynamicRegistryElementFactory<ChatType> factory() {
        return ChatType::fromNBT;
    }
}
