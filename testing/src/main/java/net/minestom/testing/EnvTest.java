package net.minestom.testing;

import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@ExtendWith(EnvTest.EnvParameterResolver.class)
@ExtendWith(EnvTest.EnvBefore.class)
@ExtendWith(EnvTest.EnvCleaner.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
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
            EnvImpl env = (EnvImpl) invocationContext.getArguments().get(0);
            env.cleanup();
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
