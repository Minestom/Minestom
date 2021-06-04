package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Object containing all the global event listeners.
 *
 * @deprecated use {@link MinecraftServer#getGlobalEventNode()}
 */
@Deprecated
public final class GlobalEventHandler implements EventHandler<Event> {

    private final EventNode<Event> node = EventNode.all("global-handler");

    {
        MinecraftServer.getGlobalEventNode().addChild(node);
    }

    @Override
    public @NotNull EventNode<Event> getEventNode() {
        return node;
    }
}
