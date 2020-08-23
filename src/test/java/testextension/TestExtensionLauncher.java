package testextension;

import net.minestom.server.Bootstrap;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

// To launch with VM arguments:
// -Dminestom.extension.indevfolder.classes=build/classes/java/test/ -Dminestom.extension.indevfolder.resources=build/resources/test/
public class TestExtensionLauncher {

    public static void main(String[] args) {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.testextension.json");
        Bootstrap.bootstrap("fr.themode.demo.MainDemo", args);
    }

}
