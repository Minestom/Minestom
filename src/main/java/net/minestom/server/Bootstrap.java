package net.minestom.server;

import net.minestom.server.extras.selfmodification.MinestomOverwriteClassLoader;
import net.minestom.server.extras.selfmodification.mixins.MixinCodeModifier;
import net.minestom.server.extras.selfmodification.mixins.MixinServiceMinestom;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.CommandLineOptions;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Used to launch Minestom with the {@link MinestomOverwriteClassLoader} to allow for self-modifications
 */
public final class Bootstrap {

    public static void bootstrap(String mainClassFullName, String[] args) {
        try {
            ClassLoader classLoader = MinestomOverwriteClassLoader.getInstance();
            startMixin(args);
            MinestomOverwriteClassLoader.getInstance().addCodeModifier(new MixinCodeModifier());

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
            main.invoke(null, new Object[] { args });
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void startMixin(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // hacks required to pass custom arguments
        Method start = MixinBootstrap.class.getDeclaredMethod("start");
        start.setAccessible(true);
        if (! ((boolean)start.invoke(null)) ) {
            return;
        }

        Method doInit = MixinBootstrap.class.getDeclaredMethod("doInit", CommandLineOptions.class);
        doInit.setAccessible(true);
        doInit.invoke(null, CommandLineOptions.ofArgs(Arrays.asList(args)));

        MixinBootstrap.getPlatform().inject();
        Mixins.getConfigs().forEach(c -> MinestomOverwriteClassLoader.getInstance().protectedPackages.add(c.getConfig().getMixinPackage()));
    }
}
