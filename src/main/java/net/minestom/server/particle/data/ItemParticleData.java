package net.minestom.server.particle.data;

import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;

public class ItemParticleData extends ParticleData {
    private final ItemStack stack;

    public ItemParticleData(ItemStack stack) {
        super(Particle.ITEM);
        this.stack = stack;
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeItemStack(stack);
    }
}
