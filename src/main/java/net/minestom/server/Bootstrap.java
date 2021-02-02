package net.minestom.server;

import net.minestom.server.extensions.ExtensionManager;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import net.minestom.server.extras.selfmodification.mixins.MixinCodeModifier;
import net.minestom.server.extras.selfmodification.mixins.MixinServiceMinestom;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.CommandLineOptions;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.ServiceNotAvailableError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Used to launch Minestom with the {@link MinestomRootClassLoader} to allow for self-modifications
 */
public final class Bootstrap {

    public static void bootstrap(String mainClassFullName, String[] args) {
        try {
            ClassLoader classLoader = MinestomRootClassLoader.getInstance();
            startMixin(args);
            try {
                MinestomRootClassLoader.getInstance().addCodeModifier(new MixinCodeModifier());
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.err.println("Failed to add MixinCodeModifier, mixins will not be injected. Check the log entries above to debug.");
            }

            ExtensionManager.loadCodeModifiersEarly();

            MixinServiceMinestom.gotoPreinitPhase();
            // ensure extensions are loaded when starting the server
            Class<?> serverClass = classLoader.loadClass("net.minestom.server.MinecraftServer");
            Method init = serverClass.getMethod("init");
            init.invoke(null);
            MixinServiceMinestom.gotoInitPhase();

            MixinServiceMinestom.gotoDefaultPhase();

            Class<?> mainClass = classLoader.loadClass(mainClassFullName);
            Method main = mainClass.getDeclaredMethod("main", String[].class);
            main.setAccessible(true);
            main.invoke(null, new Object[]{args});
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void startMixin(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // hacks required to pass custom arguments
        Method start = MixinBootstrap.class.getDeclaredMethod("start");
        start.setAccessible(true);
        try {
            if (!((boolean) start.invoke(null))) {
                return;
            }
        } catch (ServiceNotAvailableError e) {
            e.printStackTrace();
            System.err.println("Failed to load Mixin, see error above.");
            System.err.println("It is possible you simply have two files with identical names inside your server jar. " +
                    "Check your META-INF/services directory inside your Minestom implementation and merge files with identical names inside META-INF/services.");

            return;
        }

        Method doInit = MixinBootstrap.class.getDeclaredMethod("doInit", CommandLineOptions.class);
        doInit.setAccessible(true);
        doInit.invoke(null, CommandLineOptions.ofArgs(Arrays.asList(args)));

        MixinBootstrap.getPlatform().inject();
        Mixins.getConfigs().forEach(c -> MinestomRootClassLoader.getInstance().protectedPackages.add(c.getConfig().getMixinPackage()));
    }
}
