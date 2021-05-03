package net.minestom.server.particle.data;

import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemParticleEffect extends ParticleEffect {

    private final ItemStack stack;

    public ItemParticleEffect(@NotNull ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeItemStack(stack);
    }

    @Override
    public @Nullable ItemParticleEffect read(@Nullable String data) {
        if (data == null) return null;

        try {
            return new ItemParticleEffect(ArgumentItemStack.staticParse(data));
        } catch (ArgumentSyntaxException e) {
            return null;
        }
    }
}
