package net.minestom.server.extensions;

record MavenDependencyImpl(
        String groupId,
        String artifactId,
        String version,
        boolean isOptional
) implements Dependency.MavenDependency {
    @Override
    public String id() {
        return artifactId();
    }
}
