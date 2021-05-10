package net.minestom.server.extensions;

import net.minestom.dependencies.DependencyResolver;
import net.minestom.dependencies.ResolvedDependency;
import net.minestom.dependencies.UnresolvedDependencyException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Does NOT relocate extensions
 */
public class ExtensionDependencyResolver implements DependencyResolver {

    private final Map<String, DiscoveredExtension> extensionMap = new HashMap<>();

    public ExtensionDependencyResolver(@NotNull List<DiscoveredExtension> extensions) {
        for (DiscoveredExtension ext : extensions) {
            extensionMap.put(ext.getName(), ext);
        }
    }

    @NotNull
    @Override
    public ResolvedDependency resolve(@NotNull String extensionName, @NotNull File file) throws UnresolvedDependencyException {
        if (extensionMap.containsKey(extensionName)) {
            DiscoveredExtension ext = extensionMap.get(extensionName);
            // convert extension URLs to subdependencies
            // FIXME: this is not a deep conversion, this might create an issue in this scenario with different classloaders:
            // A depends on an external lib (Ext<-A)
            // B depends on A (A<-B)
            // When loading B, with no deep conversion, Ext will not be added to the list of dependencies (because it is not a direct dependency)
            // But when trying to call/access code from extension A, the parts dependent on Ext won't be inside B's dependencies, triggering a ClassNotFoundException
            List<ResolvedDependency> dependencies = new LinkedList<>();

            for (URL url : ext.files) {
                dependencies.add(new ResolvedDependency(url.toExternalForm(), url.toExternalForm(), "", url, new LinkedList<>()));
            }

            return new ResolvedDependency(ext.getName(), ext.getName(), ext.getVersion(), ext.files.get(0), dependencies);
        }
        throw new UnresolvedDependencyException("No extension named " + extensionName);
    }

    @Override
    public String toString() {
        String list = extensionMap.values()
                .stream()
                .map(DiscoveredExtension::getName)
                .collect(Collectors.joining(", "));
        return "ExtensionDependencyResolver[" + list + "]";
    }
}
