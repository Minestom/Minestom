package net.minestom.testing.environment;

import net.minestom.server.MinecraftServer;
import net.minestom.testing.Env;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

/**
 * Handles {@link Env} parameter for JUnit Tests to inject the TestEnvironment
 * @since 1.4.1
 */
public final class TestEnvironmentParameterResolver extends TypeBasedParameterResolver<Env>  {
    @Override
    public Env resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return Env.createInstance(MinecraftServer.updateProcess());
    }
}
