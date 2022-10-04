package net.minestom.server.api;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

final class EnvCleaner implements InvocationInterceptor {
    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        invocation.proceed();
        EnvImpl env = (EnvImpl) invocationContext.getArguments().get(0);
        env.cleanup();
    }
}
