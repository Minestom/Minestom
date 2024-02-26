package net.minestom.server.particle.data;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public record ItemParticleData(ItemStack item) implements ParticleData {
    ItemParticleData(NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.ITEM));
    }

    ItemParticleData() {
        this(ItemStack.of(Material.STONE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.ITEM, item);
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.ITEM.id();
    }
}
