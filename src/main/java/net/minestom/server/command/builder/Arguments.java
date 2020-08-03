package net.minestom.server.command.builder;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Enchantment;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionType;
import net.minestom.server.utils.math.FloatRange;
import net.minestom.server.utils.math.IntRange;
import net.minestom.server.utils.time.UpdateOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to retrieve argument data
 */
public class Arguments {

    private final Map<String, Object> args = new HashMap<>();

    public boolean getBoolean(String id) {
        return (boolean) getObject(id);
    }

    public long getLong(String id) {
        return (long) getObject(id);
    }

    public int getInteger(String id) {
        return (int) getObject(id);
    }

    public double getDouble(String id) {
        return (double) getObject(id);
    }

    public float getFloat(String id) {
        return (float) getObject(id);
    }

    public String getString(String id) {
        return (String) getObject(id);
    }

    public String getWord(String id) {
        return getString(id);
    }

    public String[] getStringArray(String id) {
        return (String[]) getObject(id);
    }

    public ChatColor getColor(String id) {
        return (ChatColor) getObject(id);
    }

    public UpdateOption getTime(String id) {
        return (UpdateOption) getObject(id);
    }

    public Enchantment getEnchantment(String id) {
        return (Enchantment) getObject(id);
    }

    public Particle getParticle(String id) {
        return (Particle) getObject(id);
    }

    public PotionType getPotion(String id) {
        return (PotionType) getObject(id);
    }

    public EntityType getEntityType(String id) {
        return (EntityType) getObject(id);
    }

    public IntRange getIntRange(String id) {
        return (IntRange) getObject(id);
    }

    public FloatRange getFloatRange(String id) {
        return (FloatRange) getObject(id);
    }

    public List<Entity> getEntities(String id) {
        return (List<Entity>) getObject(id);
    }

    public Object getObject(String id) {
        return args.getOrDefault(id, null);
    }

    protected void setArg(String id, Object value) {
        this.args.put(id, value);
    }

}
