package net.minestom.demo.feature.debug;

import net.minestom.demo.core.Feature;
import net.minestom.server.FeatureFlag;
import net.minestom.server.ServerProcess;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerGameModeRequestEvent;

/** Utility/debug commands, op-gated gamemode requests, translation toggles. */
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
