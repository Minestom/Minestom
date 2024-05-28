package net.minestom.testing;

import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * @deprecated As of Microtus 1.4.2, because this version don't support Non Env tests and env tests at the same time use {@link net.minestom.testing.annotations.EnvironmentTest} instead.
 */
@ExtendWith(EnvTest.EnvParameterResolver.class)
@ExtendWith(EnvTest.EnvBefore.class)
@ExtendWith(EnvTest.EnvCleaner.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated(since = "1.4.2", forRemoval = true)
public @interface EnvTest {

    final class EnvBefore implements BeforeEachCallback {
        @Override
        public void beforeEach(ExtensionContext context) {
            System.setProperty("minestom.viewable-packet", "false");
        }
    }

    final class EnvCleaner implements InvocationInterceptor {
        @Override
        public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
            invocation.proceed();
            Env env = null;
            for (Object arg : invocationContext.getArguments()) {
                if (arg instanceof Env) {
                    env = (Env) arg;
                    break;
                }
            }
            if (env instanceof EnvImpl envImpl) envImpl.cleanup();
        }
    }

    final class EnvParameterResolver extends TypeBasedParameterResolver<Env> {
        @Override
        public Env resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
                throws ParameterResolutionException {
            return new EnvImpl(MinecraftServer.updateProcess());
        }
    }
}
