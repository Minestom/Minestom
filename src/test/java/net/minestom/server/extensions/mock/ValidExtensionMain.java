package net.minestom.server.extensions.mock;

import net.minestom.server.extensions.Extension;

public class ValidExtensionMain extends Extension {
    @Override
    public LoadStatus initialize() {
        return LoadStatus.SUCCESS;
    }

    @Override
    public void terminate() {

    }
}
