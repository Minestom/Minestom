package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving a {@link PotionEffect}.
 */
public class ArgumentPotionEffect extends ArgumentRegistry<PotionEffect> {

    public ArgumentPotionEffect(String id) {
        super(id);
    }

    @Override
    public @NotNull PotionEffect parse(@NotNull StringReader input) throws CommandException {
        NamespaceID id = input.readNamespaceID();
        PotionEffect potionEffect = PotionEffect.fromNamespaceId(id);
        if (potionEffect == null){
            throw CommandException.EFFECT_EFFECTNOTFOUND.generateException(input, id.asString());
        }
        return potionEffect;
    }

    @Override
    public PotionEffect getRegistry(@NotNull String value) {
        return PotionEffect.fromNamespaceId(value);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:mob_effect";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("Potion<%s>", getId());
    }
}
