package net.minestom.demo.feature.debug;

import net.minestom.demo.core.Feature;
import net.minestom.server.FeatureFlag;
import net.minestom.server.ServerProcess;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerGameModeRequestEvent;

/**
 * Utility / debug grab-bag:
 * <ul>
 *   <li>Commands: {@code /test}, {@code /test2}, {@code /execute},
 *       {@code /redirect}, {@code /autoview}, {@code /testinstabreak},
 *       {@code /legacy}, {@code /find}, {@code /gamemode},
 *       {@code /teleport}, {@code /shutdown}, {@code /entityselector}.</li>
 *   <li>{@link PlayerGameModeRequestEvent}: honour gamemode requests from
 *       op players (permission level &ge; 2).</li>
 *   <li>{@link AsyncPlayerConfigurationEvent}: removes the
 *       {@link FeatureFlag#TRADE_REBALANCE} flag, just to demonstrate that
 *       feature flags can be toggled at config time.</li>
 *   <li>Enables Adventure's automatic component translation and installs
 *       an identity translator (no-op) so demo code paths exercising
 *       translation logic are wired up.</li>
 * </ul>
 */
public final class DebugFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(
                new TestCommand(),
                new TestCommand2(),
                new ExecuteCommand(),
                new RedirectTestCommand(),
                new AutoViewCommand(),
                new TestInstabreakCommand(),
                new LegacyCommand(),
                new FindCommand(),
                new GamemodeCommand(),
                new TeleportCommand(),
                new ShutdownCommand(),
                new EntitySelectorCommand()
        );

        process.eventHandler().addListener(AsyncPlayerConfigurationEvent.class,
                event -> event.removeFeatureFlag(FeatureFlag.TRADE_REBALANCE));

        process.eventHandler().addListener(PlayerGameModeRequestEvent.class, event -> {
            final var player = event.getPlayer();
            if (player.getPermissionLevel() >= 2) {
                player.setGameMode(event.getRequestedGameMode());
            }
        });

        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = true;
        MinestomAdventure.COMPONENT_TRANSLATOR = (c, l) -> c;
    }
}
