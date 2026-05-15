package net.minestom.demo.feature.display;

import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;

/**
 * Display entity showcase: {@code /display} — spawns block/item/text
 * display entities with various transforms.
 */
public final class DisplayFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(new DisplayCommand());
    }
}
