package net.minestom.server.registry.dynamic.chat;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.dynamic.DynamicRegistryEntry;
import net.minestom.server.registry.dynamic.DynamicRegistryEntryBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class ChatTypeBuilder implements DynamicRegistryEntryBuilder<ChatType> {
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
    public Key registry() {
        return DynamicRegistryEntry.CHAT_TYPE_REGISTRY;
    }

    @Override
    public Key name() {
        return key;
    }

    @Override
    public ChatType build(int id) {
        return new ChatTypeImpl(id, key, chat, overlay, narration);
    }
}
