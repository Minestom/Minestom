package net.minestom.server.command.builder;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeBlockPosition;
import net.minestom.server.utils.location.RelativeVec;
import net.minestom.server.utils.math.FloatRange;
import net.minestom.server.utils.math.IntRange;
import net.minestom.server.utils.time.UpdateOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to retrieve argument data in a {@link CommandExecutor}.
 * <p>
 * All id are the one specified in the {@link net.minestom.server.command.builder.arguments.Argument} constructor.
 * <p>
 * All methods are @{@link NotNull} in the sense that you should not have to verify their validity since if the syntax
 * is called, it means that all of its arguments are correct. Be aware that trying to retrieve an argument not present
 * in the syntax will result in a {@link NullPointerException}.
 */
public final class Arguments {

    private Map<String, Object> args = new HashMap<>();

    @NotNull
    public <T> T get(@NotNull Argument<T> argument) {
        return (T) getObject(argument.getId());
    }

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

    @NotNull
    public String getString(@NotNull String id) {
        return (String) getObject(id);
    }

    @NotNull
    public String getWord(@NotNull String id) {
        return getString(id);
    }

    @NotNull
    public String[] getStringArray(@NotNull String id) {
        return (String[]) getObject(id);
    }

    @NotNull
    public ChatColor getColor(@NotNull String id) {
        return (ChatColor) getObject(id);
    }

    @NotNull
    public UpdateOption getTime(@NotNull String id) {
        return (UpdateOption) getObject(id);
    }

    @NotNull
    public Enchantment getEnchantment(@NotNull String id) {
        return (Enchantment) getObject(id);
    }

    @NotNull
    public Particle getParticle(@NotNull String id) {
        return (Particle) getObject(id);
    }

    @NotNull
    public PotionEffect getPotionEffect(@NotNull String id) {
        return (PotionEffect) getObject(id);
    }

    @NotNull
    public EntityType getEntityType(@NotNull String id) {
        return (EntityType) getObject(id);
    }

    @NotNull
    public Block getBlockState(@NotNull String id) {
        return (Block) getObject(id);
    }

    @NotNull
    public IntRange getIntRange(@NotNull String id) {
        return (IntRange) getObject(id);
    }

    @NotNull
    public FloatRange getFloatRange(@NotNull String id) {
        return (FloatRange) getObject(id);
    }

    @NotNull
    public EntityFinder getEntities(@NotNull String id) {
        return (EntityFinder) getObject(id);
    }

    @NotNull
    public ItemStack getItemStack(@NotNull String id) {
        return (ItemStack) getObject(id);
    }

    @NotNull
    public NBTCompound getNbtCompound(@NotNull String id) {
        return (NBTCompound) getObject(id);
    }

    @NotNull
    public NBT getNBT(@NotNull String id) {
        return (NBT) getObject(id);
    }

    @NotNull
    public RelativeBlockPosition getRelativeBlockPosition(@NotNull String id) {
        return (RelativeBlockPosition) getObject(id);
    }

    @NotNull
    public RelativeVec getRelativeVector(@NotNull String id) {
        return (RelativeVec) getObject(id);
    }

    @NotNull
    public Object getObject(@NotNull String id) {
        return args.computeIfAbsent(id, s -> {
            throw new NullPointerException(
                    "The argument with the id '" + id + "' has no value assigned, be sure to check your arguments id, your syntax, and that you do not change the argument id dynamically.");
        });
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

    protected void retrieveDefaultValues(@Nullable Map<String, Object> defaultValuesMap) {
        if (defaultValuesMap == null)
            return;

        for (Map.Entry<String, Object> entry : defaultValuesMap.entrySet()) {
            final String key = entry.getKey();
            if (!args.containsKey(key))
                this.args.put(key, entry.getValue());
        }

    }
}
