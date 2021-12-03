package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.item.Enchantment;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an argument giving an {@link Enchantment}.
 */
public class ArgumentEnchantment extends ArgumentRegistry<Enchantment> {

    public ArgumentEnchantment(@NotNull String id) {
        super(id);
    }

    @Override
    public @Nullable Enchantment getRegistry(@NotNull String key) {
        return Enchantment.fromNamespaceId(key);
    }

    @Override
    public @NotNull CommandException createException(@NotNull StringReader input, @NotNull String id) {
        return CommandException.ENCHANTMENT_UNKNOWN.generateException(input, id);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:item_enchantment";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("Enchantment<%s>", getId());
    }
}
