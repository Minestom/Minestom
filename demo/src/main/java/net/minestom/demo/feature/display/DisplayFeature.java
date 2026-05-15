package net.minestom.demo.feature.display;

import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;

/** {@code /display} command: block/item/text display entities. */
public final class DisplayFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(new DisplayCommand());
    }
}
