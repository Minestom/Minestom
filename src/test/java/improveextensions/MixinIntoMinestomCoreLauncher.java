package improveextensions;

import net.minestom.server.Bootstrap;

// To launch with VM arguments:

// To test early Mixin injections:
// -Dminestom.extension.indevfolder.classes=build/classes/java/test/ -Dminestom.extension.indevfolder.resources=build/resources/test/improveextensions
// To test disabling early Mixin injections:
// -Dminestom.extension.disable_early_load=true -Dminestom.extension.indevfolder.classes=build/classes/java/test/ -Dminestom.extension.indevfolder.resources=build/resources/test/improveextensions/disableearlyload

// To test extension termination when the server quits:
// -Dminestom.extension.indevfolder.classes=build/classes/java/test/ -Dminestom.extension.indevfolder.resources=build/resources/test/improveextensions/unloadonstop

// To test report of failure when a mixin configuration cannot be loaded, or code modifiers are missing
// -Dminestom.extension.indevfolder.classes=build/classes/java/test/ -Dminestom.extension.indevfolder.resources=build/resources/test/improveextensions/missingmodifiers
public class MixinIntoMinestomCoreLauncher {
     public static void main(String[] args) {
            Bootstrap.bootstrap("demo.MainDemo", args);
        }
}
