package net.minestom.server.api;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

final class EnvBefore implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        System.setProperty("minestom.viewable-packet", "false");
    }
}
