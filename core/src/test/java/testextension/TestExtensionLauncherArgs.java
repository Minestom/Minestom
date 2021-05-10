package testextension;

import net.minestom.server.Bootstrap;

// To launch with VM arguments:
// -Dminestom.extension.indevfolder.classes=build/classes/java/test/ -Dminestom.extension.indevfolder.resources=build/resources/test/
public class TestExtensionLauncherArgs {

    public static void main(String[] args) {
        String[] argsWithMixins = new String[args.length+2];
        System.arraycopy(args, 0, argsWithMixins, 0, args.length);
        argsWithMixins[argsWithMixins.length-2] = "--mixin";
        argsWithMixins[argsWithMixins.length-1] = "mixins.testextension.json";
        Bootstrap.bootstrap("demo.MainDemo", argsWithMixins);
    }

}
