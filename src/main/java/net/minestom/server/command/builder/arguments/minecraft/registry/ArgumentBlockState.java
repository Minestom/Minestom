package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.NotNull;

public class ArgumentBlockState extends ArgumentRegistry<Block> {

    public ArgumentBlockState(@NotNull String id) {
        super(id);
    }

    @Override
    public Block getRegistry(String value) {
        return Registries.getBlock(value);
    }
}
