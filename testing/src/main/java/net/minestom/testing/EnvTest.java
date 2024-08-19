package net.minestom.testing;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * @deprecated As of Microtus 1.4.2, because this version doesn't support Non Env tests and env tests at the same time.
 * Use {@link net.minestom.testing.extension.MicrotusExtension MicrotusExtension} instead of the {@code @EnvTest} annotation:
 * {@code @ExtendWith(MicrotusExtension.class)}
 */
@ExtendWith(EnvTest.EnvParameterResolver.class)
@ExtendWith(EnvTest.EnvBefore.class)
@ExtendWith(EnvTest.EnvCleaner.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated(forRemoval = true, since = "1.4.2")
@ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
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
