package net.minestom.server.particle.data;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public record ItemParticleData (ItemStack item) implements ParticleData {
    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.ITEM, item);
    }
}
