package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.minecraft.SuggestionType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.registry.Registries;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving an {@link EntityType}.
 */
public class ArgumentSoundEvent extends ArgumentRegistry<SoundEvent> {

    public ArgumentSoundEvent(String id) {
        super(id);
    }

    @Override
    public SoundEvent getRegistry(@NotNull String value) {
        return Registries.getSoundEvent(value);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, true);
        argumentNode.parser = "minecraft:available_sounds";
        argumentNode.suggestionsType = SuggestionType.AVAILABLE_SOUNDS.getIdentifier();

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("SoundEvent<%s>", getId());
    }
}
