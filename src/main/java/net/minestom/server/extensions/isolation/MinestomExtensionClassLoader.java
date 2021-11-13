package net.minestom.server.extensions.isolation;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class MinestomExtensionClassLoader extends HierarchyClassLoader {
    private final Logger logger = LoggerFactory.getLogger(MinestomExtensionClassLoader.class);

    public MinestomExtensionClassLoader(String extensionName, URL[] urls) {
        super("Ext_" + extensionName, urls, MinecraftServer.class.getClassLoader());
    }

    /**
     * Returns the name of the extension linked to this classloader
     * @return the name of the extension linked to this classloader
     */
    public String getExtensionName() {
        // simply calls ClassLoader#getName as the extension name is used to name this classloader
        //  this method is simply for ease-of-use
        return getName();
    }

    /**
     * Tries to know which extension created this object, based on the classloader of the object. This can only check that the class of the object has been loaded
     * by an extension.
     *
     * While not perfect, this should detect any callback created via extension code.
     * It is possible this current version of the implementation might struggle with callbacks created through external
     * libraries, but as libraries are loaded separately for each extension, this *should not*(tm) be a problem.
     *
     * @param obj the object to get the extension of
     * @return <code>null</code> if no extension has been found, otherwise the extension name
     */
    @Nullable
    public static String findExtensionObjectOwner(@NotNull Object obj) {
        ClassLoader cl = obj.getClass().getClassLoader();
        if (cl instanceof MinestomExtensionClassLoader extensionClassLoader) {
            return extensionClassLoader.getExtensionName();
        }
        return null;
    }
}
