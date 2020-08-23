package net.minestom.server;

import net.minestom.server.extras.selfmodification.MinestomOverwriteClassLoader;
import net.minestom.server.extras.selfmodification.mixins.MixinCodeModifier;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.CommandLineOptions;
import org.spongepowered.asm.launch.platform.MixinPlatformManager;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Used to launch Minestom with the {@link MinestomOverwriteClassLoader} to allow for self-modifications
 */
public class Bootstrap {

    public static void bootstrap(String mainClassFullName, String[] args) {
        try {
            ClassLoader classLoader = MinestomOverwriteClassLoader.getInstance();
            startMixin(args);
            MinestomOverwriteClassLoader.getInstance().addCodeModifier(new MixinCodeModifier());
            MixinEnvironment.init(MixinEnvironment.Phase.DEFAULT);

            // ensure extensions are loaded when starting the server
            Class<?> serverClass = classLoader.loadClass("net.minestom.server.MinecraftServer");
            Method init = serverClass.getMethod("init");
            init.invoke(null);

            Class<?> mainClass = classLoader.loadClass(mainClassFullName);
            Method main = mainClass.getDeclaredMethod("main", String[].class);
            main.invoke(null, new Object[] { args });
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void startMixin(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method start = MixinBootstrap.class.getDeclaredMethod("start");
        start.setAccessible(true);
        if (! ((boolean)start.invoke(null)) ) {
            return;
        }

        Method doInit = MixinBootstrap.class.getDeclaredMethod("doInit", CommandLineOptions.class);
        doInit.setAccessible(true);
        doInit.invoke(null, CommandLineOptions.ofArgs(Arrays.asList(args)));

        MixinBootstrap.getPlatform().inject();
        Mixins.getConfigs().forEach(c -> {
            MinestomOverwriteClassLoader.getInstance().protectedPackages.add(c.getConfig().getMixinPackage());
        });
    }
}
