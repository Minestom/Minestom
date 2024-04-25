package net.minestom.server.particle;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ItemParticle extends ParticleImpl {
    private final @NotNull ItemStack item;

    ItemParticle(@NotNull NamespaceID namespace, int id, @NotNull ItemStack item) {
        super(namespace, id);
        this.item = item;
    }

    @Contract(pure = true)
    public @NotNull ItemParticle withItem(@NotNull ItemStack item) {
        return new ItemParticle(namespace(), id(), item);
    }

    public @NotNull ItemStack item() {
        return item;
    }

    @Override
    public @NotNull ItemParticle readData(@NotNull NetworkBuffer reader) {
        return this.withItem(reader.read(NetworkBuffer.ITEM));
    }

    @Override
    public void writeData(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.ITEM, item);
    }
}