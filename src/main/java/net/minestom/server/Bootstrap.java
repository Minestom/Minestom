package net.minestom.server;

import net.minestom.server.extras.selfmodification.MinestomOverwriteClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Used to launch Minestom with the {@link MinestomOverwriteClassLoader} to allow for self-modifications
 */
public class Bootstrap {

    public static void bootstrap(String mainClassFullName, String[] args) {
        try {
            ClassLoader classLoader = new MinestomOverwriteClassLoader(Bootstrap.class.getClassLoader());

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
}
