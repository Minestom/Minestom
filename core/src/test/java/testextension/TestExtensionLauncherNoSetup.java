package testextension;

import net.minestom.server.Bootstrap;

// To launch with VM arguments:
// -Dminestom.extension.indevfolder.classes=build/classes/java/test/ -Dminestom.extension.indevfolder.resources=build/resources/test/
public class TestExtensionLauncherNoSetup {

    public static void main(String[] args) {
        Bootstrap.bootstrap("demo.MainDemo", args);
    }

}
