package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.item.Enchantment;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving an {@link Enchantment}.
 */
public class ArgumentEnchantment extends ArgumentRegistry<Enchantment> {

    public ArgumentEnchantment(String id) {
        super(id);
    }

    @Override
    public @NotNull Enchantment parse(@NotNull StringReader input) throws CommandException {
        NamespaceID id = input.readNamespaceID();
        Enchantment enchantment = Enchantment.fromNamespaceId(id);
        if (enchantment == null){
            throw CommandException.ENCHANTMENT_UNKNOWN.generateException(input, id.asString());
        }
        return enchantment;
    }

    @Override
    public Enchantment getRegistry(@NotNull String value) {
        return Enchantment.fromNamespaceId(value);
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
