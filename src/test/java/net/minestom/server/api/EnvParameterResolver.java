package net.minestom.server.api;

import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

final class EnvParameterResolver extends TypeBasedParameterResolver<Env> {
    @Override
    public Env resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return new EnvImpl(MinecraftServer.updateProcess());
    }
}
