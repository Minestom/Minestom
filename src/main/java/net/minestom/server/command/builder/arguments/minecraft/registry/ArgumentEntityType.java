package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.minecraft.SuggestionType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving an {@link EntityType}.
 */
public class ArgumentEntityType extends ArgumentRegistry<EntityType> {

    public ArgumentEntityType(String id) {
        super(id);
    }

    @Override
    public EntityType getRegistry(@NotNull String value) {
        return EntityType.fromNamespaceId(value);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, true);
        argumentNode.parser = "minecraft:resource_location";
        argumentNode.suggestionsType = SuggestionType.SUMMONABLE_ENTITIES.getIdentifier();

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("EntityType<%s>", getId());
    }
}
