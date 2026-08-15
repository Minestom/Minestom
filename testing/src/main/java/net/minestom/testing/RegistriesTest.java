package net.minestom.testing;

import net.minestom.server.registry.Registries;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a shared, detached vanilla {@link Registries} snapshot to a test class.
 * Test and lifecycle methods may declare a {@link Registries} parameter to receive the snapshot.
 */
@Inherited
@ExtendWith(RegistriesTestExt.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegistriesTest {
}
