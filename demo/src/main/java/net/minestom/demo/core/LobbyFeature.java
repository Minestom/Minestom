package net.minestom.demo.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Default flat lobby:
 * <ul>
 *   <li>creates one {@link InstanceContainer} filled with stone up to y=40
 *       (plus one torch per chunk) and frozen at midday;</li>
 *   <li>spawns connecting players into a random known instance, so that
 *       multi-instance demos (see {@code TransferServer}) also work;</li>
 *   <li>puts players in creative with op permissions and an unknown-command
 *       callback that prints a red "Unknown command".</li>
 * </ul>
 */
public final class LobbyFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        InstanceContainer lobby = process.instance().createInstanceContainer();
        lobby.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 40, Block.STONE);
            if (unit.absoluteStart().blockY() < 40 && unit.absoluteEnd().blockY() > 40) {
                unit.modifier().setBlock(unit.absoluteStart().blockX(), 40, unit.absoluteStart().blockZ(), Block.TORCH);
            }
        });
        lobby.setChunkSupplier(LightingChunk::new);
        lobby.setTimeRate(0);
        lobby.setTime(12000);

        process.eventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            var instances = process.instance().getInstances();
            Instance instance = instances.stream()
                    .skip(ThreadLocalRandom.current().nextInt(instances.size()))
                    .findFirst().orElse(lobby);
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(new Pos(0, 40f, 0));
        });

        process.eventHandler().addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().setGameMode(GameMode.CREATIVE);
            event.getPlayer().setPermissionLevel(4);
        });

        process.command().setUnknownCommandCallback((sender, command) ->
                sender.sendMessage(Component.text("Unknown command", NamedTextColor.RED)));
    }
}
