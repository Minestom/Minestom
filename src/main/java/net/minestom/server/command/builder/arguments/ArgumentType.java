package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.arguments.minecraft.*;
import net.minestom.server.command.builder.arguments.minecraft.registry.*;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
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
    public static ArgumentLiteral Literal(@NotNull String id) {
        return new ArgumentLiteral(id);
    }

    /**
     * @see ArgumentGroup
     */
    public static ArgumentGroup Group(@NotNull String id, @NotNull Argument<?>... arguments) {
        return new ArgumentGroup(id, arguments);
    }

    /**
     * @see ArgumentLoop
     */
    @SafeVarargs
    public static <T> ArgumentLoop<T> Loop(@NotNull String id, @NotNull Argument<T>... arguments) {
        return new ArgumentLoop<>(id, arguments);
    }

    /**
     * @see ArgumentBoolean
     */
    public static ArgumentBoolean Boolean(@NotNull String id) {
        return new ArgumentBoolean(id);
    }

    /**
     * @see ArgumentInteger
     */
    public static ArgumentInteger Integer(@NotNull String id) {
        return new ArgumentInteger(id);
    }

    /**
     * @see ArgumentDouble
     */
    public static ArgumentDouble Double(@NotNull String id) {
        return new ArgumentDouble(id);
    }

    /**
     * @see ArgumentFloat
     */
    public static ArgumentFloat Float(@NotNull String id) {
        return new ArgumentFloat(id);
    }

    /**
     * @see ArgumentString
     */
    public static ArgumentString String(@NotNull String id) {
        return new ArgumentString(id);
    }

    /**
     * @see ArgumentWord
     */
    public static ArgumentWord Word(@NotNull String id) {
        return new ArgumentWord(id);
    }

    /**
     * @see ArgumentStringArray
     */
    public static ArgumentStringArray StringArray(@NotNull String id) {
        return new ArgumentStringArray(id);
    }

    /**
     * @see ArgumentCommand
     */
    public static ArgumentCommand Command(@NotNull String id) {
        return new ArgumentCommand(id);
    }

    /**
     * @see ArgumentEnum
     */
    @SuppressWarnings("rawtypes")
    public static <E extends Enum> ArgumentEnum<E> Enum(@NotNull String id, @NotNull Class<E> enumClass) {
        return new ArgumentEnum<>(id, enumClass);
    }

    // Minecraft specific arguments

    /**
     * @see ArgumentColor
     */
    public static ArgumentColor Color(@NotNull String id) {
        return new ArgumentColor(id);
    }

    /**
     * @see ArgumentTime
     */
    public static ArgumentTime Time(@NotNull String id) {
        return new ArgumentTime(id);
    }

    /**
     * @see ArgumentEnchantment
     */
    public static ArgumentEnchantment Enchantment(@NotNull String id) {
        return new ArgumentEnchantment(id);
    }

    /**
     * @see ArgumentParticle
     */
    public static ArgumentParticle Particle(@NotNull String id) {
        return new ArgumentParticle(id);
    }

    /**
     * @see ArgumentPotionEffect
     */
    public static ArgumentPotionEffect Potion(@NotNull String id) {
        return new ArgumentPotionEffect(id);
    }

    /**
     * @see ArgumentEntityType
     */
    public static ArgumentEntityType EntityType(@NotNull String id) {
        return new ArgumentEntityType(id);
    }

    /**
     * @see ArgumentBlockState
     */
    public static ArgumentBlockState BlockState(@NotNull String id) {
        return new ArgumentBlockState(id);
    }

    /**
     * @see ArgumentIntRange
     */
    public static ArgumentIntRange IntRange(@NotNull String id) {
        return new ArgumentIntRange(id);
    }

    /**
     * @see ArgumentFloatRange
     */
    public static ArgumentFloatRange FloatRange(@NotNull String id) {
        return new ArgumentFloatRange(id);
    }

    /**
     * @see ArgumentEntity
     */
    public static ArgumentEntity Entity(@NotNull String id) {
        return new ArgumentEntity(id);
    }

    /**
     * @see ArgumentItemStack
     */
    public static ArgumentItemStack ItemStack(@NotNull String id) {
        return new ArgumentItemStack(id);
    }

    /**
     * @see ArgumentComponent
     */
    public static ArgumentComponent Component(@NotNull String id) {
        return new ArgumentComponent(id);
    }

    /**
     * @see ArgumentNbtTag
     */
    public static ArgumentNbtTag NBT(@NotNull String id) {
        return new ArgumentNbtTag(id);
    }

    /**
     * @see ArgumentNbtCompoundTag
     */
    public static ArgumentNbtCompoundTag NbtCompound(@NotNull String id) {
        return new ArgumentNbtCompoundTag(id);
    }

    /**
     * @see ArgumentRelativeBlockPosition
     */
    public static ArgumentRelativeBlockPosition RelativeBlockPosition(@NotNull String id) {
        return new ArgumentRelativeBlockPosition(id);
    }

    /**
     * @see ArgumentRelativeVec3
     */
    public static ArgumentRelativeVec3 RelativeVec3(@NotNull String id) {
        return new ArgumentRelativeVec3(id);
    }

    /**
     * @see ArgumentRelativeVec2
     */
    public static ArgumentRelativeVec2 RelativeVec2(@NotNull String id) {
        return new ArgumentRelativeVec2(id);
    }

    /**
     * @see ArgumentLoop
     * @deprecated brigadier does not support long
     */
    @Deprecated
    public static ArgumentLong Long(@NotNull String id) {
        return new ArgumentLong(id);
    }

    /**
     * @see ArgumentEntity
     * @deprecated use {@link #Entity(String)}
     */
    @Deprecated
    public static ArgumentEntity Entities(@NotNull String id) {
        return new ArgumentEntity(id);
    }

    /**
     * @see ArgumentDynamicWord
     * @deprecated will be replaced soon
     */
    @Deprecated
    public static ArgumentDynamicWord DynamicWord(@NotNull String id, @NotNull SuggestionType suggestionType) {
        return new ArgumentDynamicWord(id, suggestionType);
    }

    /**
     * @see ArgumentDynamicWord
     * @deprecated will be replaced soon
     */
    @Deprecated
    public static ArgumentDynamicWord DynamicWord(@NotNull String id) {
        return DynamicWord(id, SuggestionType.ASK_SERVER);
    }

    /**
     * @see ArgumentDynamicStringArray
     * @deprecated will be replaced soon
     */
    @Deprecated
    public static ArgumentDynamicStringArray DynamicStringArray(@NotNull String id) {
        return new ArgumentDynamicStringArray(id);
    }
}
