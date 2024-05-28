package net.minestom.testing.annotations;

import net.minestom.testing.environment.TestEnvironmentCleaner;
import net.minestom.testing.environment.TestEnvironmentParameterResolver;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates a fake environment for Microtus
 * @since 1.4.2
 */
@ExtendWith(TestEnvironmentCleaner.class)
@ExtendWith(TestEnvironmentParameterResolver.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnvironmentTest {
}
