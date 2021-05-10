package net.minestom.server.extensions;

/**
 * Observes events related to extensions
 */
public interface IExtensionObserver {

    /**
     * Called before unloading an extension (that is, right after Extension#terminate and right before Extension#unload)
     * @param extensionName the name of the extension that is being unloaded
     */
    void onExtensionUnload(String extensionName);

}
