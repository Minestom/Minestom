package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an argument giving a {@link PotionEffect}.
 */
public class ArgumentPotionEffect extends ArgumentRegistry<PotionEffect> {

    public ArgumentPotionEffect(@NotNull String id) {
        super(id);
    }

    @Override
    public @Nullable PotionEffect getRegistry(@NotNull String key) {
        return PotionEffect.fromNamespaceId(key);
    }

    @Override
    public @NotNull CommandException createException(@NotNull String input, int position, @NotNull String id) {
        return CommandException.EFFECT_EFFECTNOTFOUND.generateException(input, position, id);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:mob_effect";

        nodeMaker.addNodes(argumentNode);
    }

    @Override
    public String toString() {
        return String.format("Potion<%s>", getId());
    }
}
