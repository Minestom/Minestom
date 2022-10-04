package net.minestom.server.api;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ExtendWith(EnvParameterResolver.class)
@ExtendWith(EnvBefore.class)
@ExtendWith(EnvCleaner.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnvTest {
}
