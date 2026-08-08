package net.minestom.testing;

import net.minestom.server.registry.Registries;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

final class RegistriesTestExt implements BeforeAllCallback, ParameterResolver {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(RegistriesTestExt.class);
    private static final String REGISTRIES_KEY = "minestom.registries";

    @Override
    public void beforeAll(ExtensionContext context) {
        registries(context);
    }

    @Override
    public Registries resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return registries(extensionContext);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == Registries.class;
    }

    private static Registries registries(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE).computeIfAbsent(
                REGISTRIES_KEY, _ -> Registries.vanilla(), Registries.class);
    }
}
