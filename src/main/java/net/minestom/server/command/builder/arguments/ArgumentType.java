package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.arguments.minecraft.*;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEnchantment;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentPotionEffect;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import org.jetbrains.annotations.NotNull;

/**
 * Convenient class listing all the basics {@link Argument}.
 * <p>
 * Please see the specific class documentation for further info.
 */
public class ArgumentType {

    public static ArgumentBoolean Boolean(@NotNull String id) {
        return new ArgumentBoolean(id);
    }

    public static ArgumentLong Long(@NotNull String id) {
        return new ArgumentLong(id);
    }

    public static ArgumentInteger Integer(@NotNull String id) {
        return new ArgumentInteger(id);
    }

    public static ArgumentDouble Double(@NotNull String id) {
        return new ArgumentDouble(id);
    }

    public static ArgumentFloat Float(@NotNull String id) {
        return new ArgumentFloat(id);
    }

    public static ArgumentString String(@NotNull String id) {
        return new ArgumentString(id);
    }

    public static ArgumentWord Word(@NotNull String id) {
        return new ArgumentWord(id);
    }

    public static ArgumentDynamicWord DynamicWord(@NotNull String id) {
        return new ArgumentDynamicWord(id);
    }

    public static ArgumentStringArray StringArray(@NotNull String id) {
        return new ArgumentStringArray(id);
    }

    public static ArgumentDynamicStringArray DynamicStringArray(@NotNull String id) {
        return new ArgumentDynamicStringArray(id);
    }

    // Minecraft specific arguments

    public static ArgumentColor Color(@NotNull String id) {
        return new ArgumentColor(id);
    }

    public static ArgumentTime Time(@NotNull String id) {
        return new ArgumentTime(id);
    }

    public static ArgumentEnchantment Enchantment(@NotNull String id) {
        return new ArgumentEnchantment(id);
    }

    public static ArgumentParticle Particle(@NotNull String id) {
        return new ArgumentParticle(id);
    }

    public static ArgumentPotionEffect Potion(@NotNull String id) {
        return new ArgumentPotionEffect(id);
    }

    public static ArgumentEntityType EntityType(@NotNull String id) {
        return new ArgumentEntityType(id);
    }

    public static ArgumentIntRange IntRange(@NotNull String id) {
        return new ArgumentIntRange(id);
    }

    public static ArgumentFloatRange FloatRange(@NotNull String id) {
        return new ArgumentFloatRange(id);
    }

    @Deprecated
    public static ArgumentEntities Entities(@NotNull String id) {
        return new ArgumentEntities(id);
    }

    public static ArgumentItemStack ItemStack(@NotNull String id) {
        return new ArgumentItemStack(id);
    }

    public static ArgumentNbtCompoundTag NbtCompound(@NotNull String id) {
        return new ArgumentNbtCompoundTag(id);
    }

    public static ArgumentNbtTag NBT(@NotNull String id) {
        return new ArgumentNbtTag(id);
    }

}
