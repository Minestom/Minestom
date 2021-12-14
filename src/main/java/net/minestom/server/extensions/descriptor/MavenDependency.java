package net.minestom.server.extensions.descriptor;

public record MavenDependency(
        String groupId,
        String artifactId,
        String version,
        boolean isOptional
) implements Dependency {
    @Override
    public String id() {
        return artifactId();
    }
}
