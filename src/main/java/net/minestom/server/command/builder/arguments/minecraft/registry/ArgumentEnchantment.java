package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.item.Enchantment;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving an {@link Enchantment}.
 */
public class ArgumentEnchantment extends ArgumentRegistry<Enchantment> {

    public ArgumentEnchantment(String id) {
        super(id);
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
