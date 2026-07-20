package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.attribute.AmbientParticle;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.minestom.server.world.biome.Biome;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TestBiomeAmbientParticleCommand extends Command {

    public TestBiomeAmbientParticleCommand() {
        super("testbiomeambientparticle");
        setDefaultExecutor(TestBiomeAmbientParticleCommand::usage);
    }

    private static void usage(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command is only available for players"));
            return;
        }
        Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        Particle particle = Particle.BLOCK_MARKER.withBlock(
                Block.COPPER_BULB
                        .withProperty("lit", "true")
                        .withProperty("powered", "false")
        );
        Biome biome = Biome.builder()
                .setAttribute(EnvironmentAttribute.AMBIENT_PARTICLES, List.of(new AmbientParticle(particle, 0.005f)))
                .build();
        RegistryKey<Biome> key = MinecraftServer.getBiomeRegistry().register("testbiome", biome);
        instance.setGenerator(unit -> {
            unit.modifier().fillBiome(key);
            unit.fork(unit.absoluteStart().withY(63), unit.absoluteEnd().withY(63)).modifier().fill(Block.STONE);
        });
        // register the biome on the client side
        player.startConfigurationPhase();
        AtomicReference<EventListener<AsyncPlayerConfigurationEvent>> handlerRef = new AtomicReference<>();
        EventListener<AsyncPlayerConfigurationEvent> handler = EventListener.builder(AsyncPlayerConfigurationEvent.class).handler(event -> {
            event.setSendRegistryData(true);
            player.eventNode().removeListener(handlerRef.get());
            player.scheduler().scheduleNextTick(() -> player.setInstance(instance).join());
        }).build();
        handlerRef.set(handler);
        player.eventNode().addListener(handler);
    }
}
