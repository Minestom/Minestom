package net.minestom.server.api;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Inherited;

@ExtendWith(EnvParameterResolver.class)
@Inherited
public @interface EnvTest {
}
