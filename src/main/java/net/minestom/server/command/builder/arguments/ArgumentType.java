package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.arguments.minecraft.*;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.command.builder.parser.ArgumentParser;
import org.jetbrains.annotations.ApiStatus;

/**
 * Convenient class listing all the basics {@link Argument}.
 * <p>
 * Please see the specific class documentation for further info.
 */
public class ArgumentType {

    /**
     * @see ArgumentLiteral
     */
    public static ArgumentLiteral Literal(String id) {
        return new ArgumentLiteral(id);
    }

    /**
     * @see ArgumentGroup
     */
    public static ArgumentGroup Group(String id, Argument<?>... arguments) {
        return new ArgumentGroup(id, arguments);
    }

    /**
     * @see ArgumentLoop
     */
    @SafeVarargs
    public static <T> ArgumentLoop<T> Loop(String id, Argument<T>... arguments) {
        return new ArgumentLoop<>(id, arguments);
    }

    /**
     * @see ArgumentBoolean
     */
    public static ArgumentBoolean Boolean(String id) {
        return new ArgumentBoolean(id);
    }

    /**
     * @see ArgumentInteger
     */
    public static ArgumentInteger Integer(String id) {
        return new ArgumentInteger(id);
    }

    /**
     * @see ArgumentDouble
     */
    public static ArgumentDouble Double(String id) {
        return new ArgumentDouble(id);
    }

    /**
     * @see ArgumentFloat
     */
    public static ArgumentFloat Float(String id) {
        return new ArgumentFloat(id);
    }

    /**
     * @see ArgumentString
     */
    public static ArgumentString String(String id) {
        return new ArgumentString(id);
    }

    /**
     * @see ArgumentWord
     */
    public static ArgumentWord Word(String id) {
        return new ArgumentWord(id);
    }

    /**
     * @see ArgumentStringArray
     */
    public static ArgumentStringArray StringArray(String id) {
        return new ArgumentStringArray(id);
    }

    /**
     * @see ArgumentCommand
     */
    public static ArgumentCommand Command(String id) {
        return new ArgumentCommand(id);
    }

    /**
     * @see ArgumentEnum
     */
    @SuppressWarnings("rawtypes")
    public static <E extends Enum> ArgumentEnum<E> Enum(String id, Class<E> enumClass) {
        return new ArgumentEnum<>(id, enumClass);
    }

    // Minecraft specific arguments

    /**
     * @see ArgumentColor
     */
    public static ArgumentColor Color(String id) {
        return new ArgumentColor(id);
    }

    /**
     * @see ArgumentTime
     */
    public static ArgumentTime Time(String id) {
        return new ArgumentTime(id);
    }

    /**
     * @see ArgumentParticle
     */
    public static ArgumentParticle Particle(String id) {
        return new ArgumentParticle(id);
    }

    /**
     * @see ArgumentResource
     */
    public static ArgumentResource Resource(String id, String identifier) {
        return new ArgumentResource(id, identifier);
    }

    /**
     * @see ArgumentResourceLocation
     */
    public static ArgumentResourceLocation ResourceLocation(String id) {
        return new ArgumentResourceLocation(id);
    }

    /**
     * @see ArgumentResourceOrTag
     */
    public static ArgumentResourceOrTag ResourceOrTag(String id, String identifier) {
        return new ArgumentResourceOrTag(id, identifier);
    }

    /**
     * @see ArgumentEntityType
     */
    public static ArgumentEntityType EntityType(String id) {
        return new ArgumentEntityType(id);
    }

    /**
     * @see ArgumentBlockState
     */
    public static ArgumentBlockState BlockState(String id) {
        return new ArgumentBlockState(id);
    }

    /**
     * @see ArgumentIntRange
     */
    public static ArgumentIntRange IntRange(String id) {
        return new ArgumentIntRange(id);
    }

    /**
     * @see ArgumentFloatRange
     */
    public static ArgumentFloatRange FloatRange(String id) {
        return new ArgumentFloatRange(id);
    }

    /**
     * @see ArgumentEntity
     */
    public static ArgumentEntity Entity(String id) {
        return new ArgumentEntity(id);
    }

    /**
     * @see ArgumentItemStack
     */
    public static ArgumentItemStack ItemStack(String id) {
        return new ArgumentItemStack(id);
    }

    /**
     * @see ArgumentComponent
     */
    public static ArgumentComponent Component(String id) {
        return new ArgumentComponent(id);
    }

    /**
     * @see ArgumentUUID
     */
    public static ArgumentUUID UUID(String id) {
        return new ArgumentUUID(id);
    }

    /**
     * @see ArgumentNbtTag
     */
    public static ArgumentNbtTag NBT(String id) {
        return new ArgumentNbtTag(id);
    }

    /**
     * @see ArgumentNbtCompoundTag
     */
    public static ArgumentNbtCompoundTag NbtCompound(String id) {
        return new ArgumentNbtCompoundTag(id);
    }

    /**
     * @see ArgumentRelativeBlockPosition
     */
    public static ArgumentRelativeBlockPosition RelativeBlockPosition(String id) {
        return new ArgumentRelativeBlockPosition(id);
    }

    /**
     * @see ArgumentRelativeVec3
     */
    public static ArgumentRelativeVec3 RelativeVec3(String id) {
        return new ArgumentRelativeVec3(id);
    }

    /**
     * @see ArgumentRelativeVec2
     */
    public static ArgumentRelativeVec2 RelativeVec2(String id) {
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
    public static Argument<?>[] generate(String format) {
        return ArgumentParser.generate(format);
    }

    /**
     * @see ArgumentLong
     */
    public static ArgumentLong Long(String id) {
        return new ArgumentLong(id);
    }

    /**
     * @see ArgumentEntity
     * @deprecated use {@link #Entity(String)}
     */
    @Deprecated
    public static ArgumentEntity Entities(String id) {
        return new ArgumentEntity(id);
    }
}
