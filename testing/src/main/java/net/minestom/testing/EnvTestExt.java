package net.minestom.testing;

import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

final class EnvTestExt implements
        BeforeEachCallback,
        AfterEachCallback,
        ParameterResolver {

    private static final String ENV_KEY = "minestom.env";

    @Override
    public void beforeEach(ExtensionContext context) {
        System.setProperty("minestom.viewable-packet", "false");
    }

    @Override
    public Env resolveParameter(ParameterContext parameterContext,
                                ExtensionContext extensionContext) {
        return extensionContext.getStore(ExtensionContext.Namespace.create(getClass()))
                .computeIfAbsent(ENV_KEY,
                        _ -> new EnvImpl(MinecraftServer.updateProcess()),
                        EnvImpl.class);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.create(getClass()));
        EnvImpl env = store.remove(ENV_KEY, EnvImpl.class);
        if (env != null) env.cleanup();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == Env.class;
    }
}
