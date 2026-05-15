package net.minestom.demo.feature.transfer;

import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;

/** {@code /transfer} and {@code /cookie} for cross-server hopping. */
public final class TransferFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(new TransferCommand(), new CookieCommand());
    }
}
