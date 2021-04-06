package net.minestom.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures how the tests inside a class denoted with this annotation are run.
 *
 * <ul>
 *     <li><code>independent</code> controls whether each test is run inside its own Minestom server.
 *     The default behaviour for tests is the same as setting it to <code>false</code></li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MinestomTestCollection {
    boolean independent() default false;

    String value() default "";
}
