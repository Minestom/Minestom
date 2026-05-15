package net.minestom.demo.feature.transfer;

import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;

/**
 * Cross-server showcase: {@code /transfer} hops to another server,
 * {@code /cookie} reads/writes the persistent cookie payload that the
 * client carries across transfers.
 */
public final class TransferFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(new TransferCommand(), new CookieCommand());
    }
}
