package net.minestom.server.particle.data;

import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.ParticleType;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class ItemParticle extends Particle {
    public static final BiFunction<ParticleType<ItemParticle>, @Nullable String, ItemParticle> READER = (particle, data) -> {
        if (data == null) return null;

        try {
            return new ItemParticle(ArgumentItemStack.staticParse(data),
                    true, 0, 0, 0, 0, 1);
        } catch (ArgumentSyntaxException e) {
            return null;
        }
    };

    private final ItemStack stack;

    public ItemParticle(@NotNull ItemStack stack, boolean longDistance,
                        float offsetX, float offsetY, float offsetZ, float speed, int count) {
        super(ParticleType.ITEM, longDistance, offsetX, offsetY, offsetZ, speed, count);
        this.stack = stack;
    }

    public ItemParticle(@NotNull ItemStack stack) {
        super(ParticleType.ITEM);
        this.stack = stack;
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeItemStack(stack);
    }
}
