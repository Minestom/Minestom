package net.minestom.server.particle.data;

import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class ItemParticleData extends ParticleData {
    public static final BiFunction<Particle<ItemParticleData>, @Nullable String, ItemParticleData> READER = (particle, data) -> {
        if (data == null) return null;

        try {
            return new ItemParticleData(ArgumentItemStack.staticParse(data));
        } catch (ArgumentSyntaxException e) {
            return null;
        }
    };

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
