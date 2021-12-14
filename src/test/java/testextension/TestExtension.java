package testextension;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;

public class TestExtension extends Extension {

    @Override
    public LoadStatus preInitialize() {
        System.out.println("During preinit");
        MinecraftServer.setTerminalEnabled(false);
        System.out.println("Mwahaha i disabled the terminal");

        return LoadStatus.SUCCESS;
    }

    @Override
    public void initialize() {
        System.out.println("Hello from extension!");
    }

    @Override
    public void terminate() {

    }
}
