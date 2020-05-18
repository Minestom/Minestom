package net.minestom.server.gamedata.conditions;

import net.minestom.server.data.Data;
import net.minestom.server.gamedata.Condition;

import java.util.Random;

/**
 * Requires 'explosionPower' double argument
 */
public class SurvivesExplosionCondition implements Condition {
    private Random rng = new Random();

    @Override
    public boolean test(Data data) {
        if(data == null)
            return true; // no explosion here
        if(!data.hasKey("explosionPower"))
            return true; // no explosion here
        return rng.nextDouble() <= 1.0/data.<Double>get("explosionPower");
    }
}
