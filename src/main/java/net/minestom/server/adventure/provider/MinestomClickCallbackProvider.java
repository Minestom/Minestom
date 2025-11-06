package net.minestom.server.adventure.provider;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.MinecraftServer;

@SuppressWarnings("UnstableApiUsage") // we are permitted to provide this
public class MinestomClickCallbackProvider implements ClickCallback.Provider {
    @Override
    public ClickEvent create(ClickCallback<Audience> callback, ClickCallback.Options options) {
        return MinecraftServer.getClickCallbackManager().createClickEvent(callback, options);
    }
}
