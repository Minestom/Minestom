package net.minestom.server.particle.data;

import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class ItemParticleEffect extends ParticleEffect {

    private final ItemStack stack;

    public ItemParticleEffect(@NotNull ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeItemStack(stack);
    }

    @Override
    public @Nullable ItemParticleEffect read(@Nullable Scanner data) {
        if (data == null) return null;

        try {
            return new ItemParticleEffect(ArgumentItemStack.staticParse(data.next()));
        } catch (ArgumentSyntaxException | NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public @Nullable ParticleEffect read(@NotNull BinaryReader reader) {
        return new ItemParticleEffect(reader.readItemStack());
    }
}
