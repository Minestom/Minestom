package net.minestom.server.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.entity.EntityPotionAddEvent;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityCancellableEffectTest
{

    static {
        MinecraftServer.init();
    }

    @Test
    public void cancelEffect(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        LivingEntity entity = new LivingEntity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Vec(0, 0, 0));

        Potion potion = new Potion(PotionEffect.ABSORPTION, 0, Potion.INFINITE_DURATION);
        MinecraftServer.getGlobalEventHandler().addListener(EntityPotionAddEvent.class, event -> event.setCancelled(true));
        entity.addEffect(potion);

        assertFalse(entity.hasEffect(PotionEffect.ABSORPTION));
    }

}
