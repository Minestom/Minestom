package net.minestom.testing.extension;

import net.minestom.server.MinecraftServer;
import net.minestom.testing.Env;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The {@code MicrotusExtension} class extends {@link TypeBasedParameterResolver<Env>} and implements {@link InvocationInterceptor}.
 * This extension is used to resolve parameters of type {@link Env} and to intercept test method invocations.
 *
 * @since 1.5.0
 */
public class MicrotusExtension extends TypeBasedParameterResolver<Env> implements InvocationInterceptor {

    /**
     * Resolves the parameter of type {@link Env}.
     *
     * @param parameterContext the context for the parameter for which an argument should be resolved; never {@code null}
     * @param extensionContext the extension context for the {@code Executable} about to be invoked; never {@code null}
     * @return an instance of {@link Env}
     * @throws ParameterResolutionException if an error occurs during parameter resolution
     */
    @Override
    public Env resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return Env.createInstance(MinecraftServer.updateProcess());
    }

    /**
     * Intercepts the test method invocation to perform additional actions before or after the test method execution.
     *
     * @param invocation the invocation to be intercepted; never {@code null}
     * @param invocationContext the context for the reflective invocation of the test method; never {@code null}
     * @param extensionContext the context for the extension; never {@code null}
     * @throws Throwable if an error occurs during the interception
     */
    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        invocation.proceed();
        List<Object> arguments = invocationContext.getArguments();
        arguments.stream().filter(Env.class::isInstance).findFirst().ifPresent(o -> {
            Env env = (Env) o;
            env.cleanup();
        });
    }
}
