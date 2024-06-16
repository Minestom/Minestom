package net.minestom.testing.annotations;

import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.ApiStatus;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates a fake environment for Microtus
 * @since 1.4.2
 * @deprecated As of Microtus 1.5.0, because better and deeper integration of JUnit5 testing use
 *             {@link net.minestom.testing.extension.MicrotusExtension} instead.
 */
@ExtendWith(MicrotusExtension.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated(forRemoval = true, since = "1.5.0")
@ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
public @interface EnvironmentTest {
}
