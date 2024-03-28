package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.minecraft.SuggestionType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.registry.Registries;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving an {@link Sound.Type}.
 */
public class ArgumentSound extends ArgumentRegistry<Sound.Type> {

    public ArgumentSound(String id) {
        super(id);
    }

    @Override
    public Sound.Type getRegistry(@NotNull String value) {
        SoundEvent soundEvent = Registries.getSoundEvent(value);

        if (soundEvent == null) {
            return () -> Key.key(value); // Instance of Sound.Type
        }

        return soundEvent;
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
        return String.format("Sound<%s>", getId());
    }
}
