package net.minestom.server.extensions.descriptor;

import net.minestom.server.extensions.DiscoveredExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ExtensionDescriptor() {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveredExtension.class);

    static final String NAME_REGEX = "[A-Za-z][_A-Za-z0-9]+";
}
