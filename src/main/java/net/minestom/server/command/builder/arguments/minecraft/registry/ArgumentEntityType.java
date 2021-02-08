package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.registry.Registries;
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
        return Registries.getEntityType(value);
    }

    @NotNull
    @Override
    public DeclareCommandsPacket.Node[] toNodes(boolean executable) {
        DeclareCommandsPacket.Node argumentNode = MinecraftServer.getCommandManager().simpleArgumentNode(this, executable, false);
        argumentNode.parser = "minecraft:entity_summon";
        return new DeclareCommandsPacket.Node[]{argumentNode};
    }
}
