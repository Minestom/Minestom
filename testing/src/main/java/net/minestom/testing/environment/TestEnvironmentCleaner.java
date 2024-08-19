package net.minestom.testing.environment;

import net.minestom.testing.Env;
import org.jetbrains.annotations.ApiStatus;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Handles {@link Env} to clean the Test Environment after usage
 * @since 1.4.2
 */
@Deprecated(forRemoval = true, since = "1.5.0")
@ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
public final class TestEnvironmentCleaner implements InvocationInterceptor {
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
