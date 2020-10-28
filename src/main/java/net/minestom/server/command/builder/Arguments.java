package net.minestom.server.command.builder;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.math.FloatRange;
import net.minestom.server.utils.math.IntRange;
import net.minestom.server.utils.time.UpdateOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to retrieve argument data.
 */
public class Arguments {

    private Map<String, Object> args = new HashMap<>();

    @Nullable
    public boolean getBoolean(@NotNull String id) {
        return (boolean) getObject(id);
    }

    public long getLong(@NotNull String id) {
        return (long) getObject(id);
    }

    public int getInteger(@NotNull String id) {
        return (int) getObject(id);
    }

    public double getDouble(@NotNull String id) {
        return (double) getObject(id);
    }

    public float getFloat(@NotNull String id) {
        return (float) getObject(id);
    }

    @Nullable
    public String getString(@NotNull String id) {
        return (String) getObject(id);
    }

    @Nullable
    public String getWord(@NotNull String id) {
        return getString(id);
    }

    @Nullable
    public String[] getStringArray(@NotNull String id) {
        return (String[]) getObject(id);
    }

    @Nullable
    public ChatColor getColor(@NotNull String id) {
        return (ChatColor) getObject(id);
    }

    @Nullable
    public UpdateOption getTime(@NotNull String id) {
        return (UpdateOption) getObject(id);
    }

    @Nullable
    public Enchantment getEnchantment(@NotNull String id) {
        return (Enchantment) getObject(id);
    }

    @Nullable
    public Particle getParticle(@NotNull String id) {
        return (Particle) getObject(id);
    }

    @Nullable
    public PotionEffect getPotionEffect(@NotNull String id) {
        return (PotionEffect) getObject(id);
    }

    @Nullable
    public EntityType getEntityType(@NotNull String id) {
        return (EntityType) getObject(id);
    }

    @Nullable
    public IntRange getIntRange(@NotNull String id) {
        return (IntRange) getObject(id);
    }

    @Nullable
    public FloatRange getFloatRange(@NotNull String id) {
        return (FloatRange) getObject(id);
    }

    @Nullable
    public List<Entity> getEntities(@NotNull String id) {
        return (List<Entity>) getObject(id);
    }

    @Nullable
    public ItemStack getItemStack(@NotNull String id) {
        return (ItemStack) getObject(id);
    }

    @Nullable
    public NBTCompound getNbtCompound(@NotNull String id) {
        return (NBTCompound) getObject(id);
    }

    @Nullable
    public NBT getNBT(@NotNull String id) {
        return (NBT) getObject(id);
    }

    @Nullable
    public Object getObject(@NotNull String id) {
        return args.getOrDefault(id, null);
    }

    protected void setArg(@NotNull String id, Object value) {
        this.args.put(id, value);
    }

    protected void copy(Arguments arguments) {
        this.args = arguments.args;
    }

    protected void clear() {
        this.args.clear();
    }

}
