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

    public static ArgumentLiteral Literal(@NotNull String id) {
        return new ArgumentLiteral(id);
    }

    public static ArgumentGroup Group(@NotNull String id, @NotNull Argument<?>... arguments) {
        return new ArgumentGroup(id, arguments);
    }

    @SafeVarargs
    public static <T> ArgumentLoop<T> Loop(@NotNull String id, @NotNull Argument<T>... arguments) {
        return new ArgumentLoop<>(id, arguments);
    }

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

    public static ArgumentDynamicWord DynamicWord(@NotNull String id, @NotNull SuggestionType suggestionType) {
        return new ArgumentDynamicWord(id, suggestionType);
    }

    public static ArgumentDynamicWord DynamicWord(@NotNull String id) {
        return DynamicWord(id, SuggestionType.ASK_SERVER);
    }

    public static ArgumentStringArray StringArray(@NotNull String id) {
        return new ArgumentStringArray(id);
    }

    public static ArgumentDynamicStringArray DynamicStringArray(@NotNull String id) {
        return new ArgumentDynamicStringArray(id);
    }

    public static ArgumentCommand Command(@NotNull String id) {
        return new ArgumentCommand(id);
    }

    @SuppressWarnings("rawtypes")
    public static <E extends Enum> ArgumentEnum<E> Enum(@NotNull String id, @NotNull Class<E> enumClass) {
        return new ArgumentEnum<>(id, enumClass);
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

    public static ArgumentBlockState BlockState(@NotNull String id) {
        return new ArgumentBlockState(id);
    }

    public static ArgumentIntRange IntRange(@NotNull String id) {
        return new ArgumentIntRange(id);
    }

    public static ArgumentFloatRange FloatRange(@NotNull String id) {
        return new ArgumentFloatRange(id);
    }

    public static ArgumentEntity Entities(@NotNull String id) {
        return new ArgumentEntity(id);
    }

    public static ArgumentItemStack ItemStack(@NotNull String id) {
        return new ArgumentItemStack(id);
    }

    public static ArgumentComponent Component(@NotNull String id) {
        return new ArgumentComponent(id);
    }

    public static ArgumentNbtTag NBT(@NotNull String id) {
        return new ArgumentNbtTag(id);
    }

    public static ArgumentNbtCompoundTag NbtCompound(@NotNull String id) {
        return new ArgumentNbtCompoundTag(id);
    }

    public static ArgumentRelativeBlockPosition RelativeBlockPosition(@NotNull String id) {
        return new ArgumentRelativeBlockPosition(id);
    }

    public static ArgumentRelativeVec3 RelativeVec3(@NotNull String id) {
        return new ArgumentRelativeVec3(id);
    }

    public static ArgumentRelativeVec2 RelativeVec2(@NotNull String id) {
        return new ArgumentRelativeVec2(id);
    }

}
