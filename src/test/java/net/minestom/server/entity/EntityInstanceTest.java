package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvParameterResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EnvParameterResolver.class)
public class EntityInstanceTest {

    @Test
    public void entityJoin(Env env) {
        var instance = env.process().instance().createInstanceContainer();
        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance).join();
        assertEquals(instance, entity.getInstance());
    }

    @Test
    public void playerJoin(Env env) {
        var instance = env.process().instance().createInstanceContainer();
        var connection = env.createConnection();
        var player = connection.connect(instance).join();
        assertEquals(instance, player.getInstance());
    }
}
