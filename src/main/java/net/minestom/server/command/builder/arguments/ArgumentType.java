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
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.command.builder.parser.ArgumentParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Convenient class listing all the basics {@link Argument}.
 * <p>
 * Please see the specific class documentation for further info.
 */
public class ArgumentType {

    /**
     * @see ArgumentLiteral
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentLiteral Literal(@NotNull String id) {
        return new ArgumentLiteral(id);
    }

    /**
     * @see ArgumentGroup
     */
    @Contract("_, _ -> new")
    public static @NotNull ArgumentGroup Group(@NotNull String id, @NotNull Argument<?>... arguments) {
        return new ArgumentGroup(id, arguments);
    }

    /**
     * @see ArgumentLoop
     */
    @Contract("_, _ -> new")
    @SafeVarargs
    public static <T> @NotNull ArgumentLoop<T> Loop(@NotNull String id, @NotNull Argument<T>... arguments) {
        return new ArgumentLoop<>(id, arguments);
    }

    /**
     * @see ArgumentBoolean
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentBoolean Boolean(@NotNull String id) {
        return new ArgumentBoolean(id);
    }

    /**
     * @see ArgumentInteger
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentInteger Integer(@NotNull String id) {
        return new ArgumentInteger(id);
    }

    /**
     * @see ArgumentDouble
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentDouble Double(@NotNull String id) {
        return new ArgumentDouble(id);
    }

    /**
     * @see ArgumentFloat
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentFloat Float(@NotNull String id) {
        return new ArgumentFloat(id);
    }

    /**
     * @see ArgumentString
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentString String(@NotNull String id) {
        return new ArgumentString(id);
    }

    /**
     * @see ArgumentWord
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentWord Word(@NotNull String id) {
        return new ArgumentWord(id);
    }

    /**
     * @see ArgumentStringArray
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentStringArray StringArray(@NotNull String id) {
        return new ArgumentStringArray(id);
    }

    /**
     * @see ArgumentCommand
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentCommand Command(@NotNull String id) {
        return new ArgumentCommand(id);
    }

    /**
     * @see ArgumentEnum
     */
    @Contract("_, _ -> new")
    @SuppressWarnings("rawtypes")
    public static <E extends Enum> @NotNull ArgumentEnum<E> Enum(@NotNull String id, @NotNull Class<E> enumClass) {
        return new ArgumentEnum<>(id, enumClass);
    }

    // Minecraft specific arguments

    /**
     * @see ArgumentColor
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentColor Color(@NotNull String id) {
        return new ArgumentColor(id);
    }

    /**
     * @see ArgumentTime
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentTime Time(@NotNull String id) {
        return new ArgumentTime(id);
    }

    /**
     * @see ArgumentEnchantment
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentEnchantment Enchantment(@NotNull String id) {
        return new ArgumentEnchantment(id);
    }

    /**
     * @see ArgumentParticle
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentParticle Particle(@NotNull String id) {
        return new ArgumentParticle(id);
    }

    /**
     * @see ArgumentResourceLocation
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentResourceLocation ResourceLocation(@NotNull String id) {
        return new ArgumentResourceLocation(id);
    }

    /**
     * @see ArgumentPotionEffect
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentPotionEffect Potion(@NotNull String id) {
        return new ArgumentPotionEffect(id);
    }

    /**
     * @see ArgumentEntityType
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentEntityType EntityType(@NotNull String id) {
        return new ArgumentEntityType(id);
    }

    /**
     * @see ArgumentBlockState
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentBlockState BlockState(@NotNull String id) {
        return new ArgumentBlockState(id);
    }

    /**
     * @see ArgumentIntRange
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentIntRange IntRange(@NotNull String id) {
        return new ArgumentIntRange(id);
    }

    /**
     * @see ArgumentFloatRange
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentFloatRange FloatRange(@NotNull String id) {
        return new ArgumentFloatRange(id);
    }

    /**
     * @see ArgumentEntity
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentEntity Entity(@NotNull String id) {
        return new ArgumentEntity(id);
    }

    /**
     * @see ArgumentItemStack
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentItemStack ItemStack(@NotNull String id) {
        return new ArgumentItemStack(id);
    }

    /**
     * @see ArgumentComponent
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentComponent Component(@NotNull String id) {
        return new ArgumentComponent(id);
    }

    /**
     * @see ArgumentUUID
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentUUID UUID(@NotNull String id) {
        return new ArgumentUUID(id);
    }

    /**
     * @see ArgumentNbtTag
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentNbtTag NBT(@NotNull String id) {
        return new ArgumentNbtTag(id);
    }

    /**
     * @see ArgumentNbtCompoundTag
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentNbtCompoundTag NbtCompound(@NotNull String id) {
        return new ArgumentNbtCompoundTag(id);
    }

    /**
     * @see ArgumentRelativeBlockPosition
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentRelativeBlockPosition RelativeBlockPosition(@NotNull String id) {
        return new ArgumentRelativeBlockPosition(id);
    }

    /**
     * @see ArgumentRelativeVec3
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentRelativeVec3 RelativeVec3(@NotNull String id) {
        return new ArgumentRelativeVec3(id);
    }

    /**
     * @see ArgumentRelativeVec2
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentRelativeVec2 RelativeVec2(@NotNull String id) {
        return new ArgumentRelativeVec2(id);
    }

    /**
     * Generates arguments from a string format.
     * <p>
     * Example: "Entity&lt;targets&gt; Integer&lt;number&gt;"
     * <p>
     * Note: this feature is in beta and is very likely to change depending on feedback.
     */
    @ApiStatus.Experimental
    public static Argument<?>[] generate(@NotNull String format) {
        return ArgumentParser.generate(format);
    }

    /**
     * @see ArgumentLong
     */
    @Contract("_ -> new")
    public static @NotNull ArgumentLong Long(@NotNull String id) {
        return new ArgumentLong(id);
    }

    /**
     * @see ArgumentEntity
     * @deprecated use {@link #Entity(String)}
     */
    @Contract("_ -> new")
    @Deprecated
    public static @NotNull ArgumentEntity Entities(@NotNull String id) {
        return new ArgumentEntity(id);
    }
}
