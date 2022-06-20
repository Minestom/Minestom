package net.minestom.server.registry.dynamic.chat;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

@ApiStatus.Internal
public class DynamicChatTypeImpl implements ChatType {
    private final Key key;
    ChatType backingType;

    public DynamicChatTypeImpl(Key key) {
        this.key = key;
    }

    @Override
    public TextDisplay chat() {
        return backingType.chat();
    }

    @Override
    public TextDisplay overlay() {
        return backingType.overlay();
    }

    @Override
    public Narration narration() {
        return backingType.narration();
    }

    @Override
    public NBTCompound toNBT() {
        return backingType.toNBT();
    }

    @Override
    public @NotNull NamespaceID namespace() {
        return NamespaceID.from(key);
    }

    @Override
    public int id() {
        return backingType.id();
    }

    @ApiStatus.Internal
    public void setBackingType(ChatType backingType) {
        this.backingType = backingType;
    }
}
