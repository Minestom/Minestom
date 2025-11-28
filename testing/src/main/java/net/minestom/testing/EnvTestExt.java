package net.minestom.testing;

import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.extension.*;

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
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) {
        return extensionContext.getStore(ExtensionContext.Namespace.create(getClass()))
                .getOrComputeIfAbsent(ENV_KEY,
                        key -> new EnvImpl(MinecraftServer.updateProcess()),
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
