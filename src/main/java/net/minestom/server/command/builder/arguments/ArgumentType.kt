package net.minestom.server.command.builder.arguments

import org.jglrxavpok.hephaistos.parser.SNBTParser.parse
import net.minestom.server.command.builder.arguments.number.ArgumentNumber
import java.util.function.BiConsumer
import net.minestom.server.utils.binary.BinaryWriter
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import java.lang.NumberFormatException
import java.lang.NullPointerException
import net.minestom.server.command.builder.NodeMaker
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket
import java.math.BigDecimal
import net.minestom.server.utils.location.RelativeVec
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec
import net.minestom.server.utils.location.RelativeVec.CoordinateType
import net.minestom.server.coordinate.Vec
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentRegistry
import net.minestom.server.entity.EntityType
import net.minestom.server.item.Enchantment
import net.minestom.server.potion.PotionEffect
import java.time.temporal.TemporalUnit
import net.minestom.server.command.builder.arguments.minecraft.ArgumentTime
import it.unimi.dsi.fastutil.chars.CharList
import it.unimi.dsi.fastutil.chars.CharArrayList
import java.lang.IllegalArgumentException
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.arguments.minecraft.ArgumentColor
import net.minestom.server.command.builder.arguments.minecraft.ArgumentRange
import net.minestom.server.utils.entity.EntityFinder
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import java.lang.StringBuilder
import net.minestom.server.entity.GameMode
import net.minestom.server.utils.entity.EntityFinder.EntitySort
import net.minestom.server.command.builder.arguments.minecraft.ArgumentIntRange
import java.lang.IllegalStateException
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTException
import net.minestom.server.command.builder.arguments.minecraft.ArgumentNbtTag
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minestom.server.command.builder.arguments.minecraft.ArgumentComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack
import net.minestom.server.item.Material
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState
import net.minestom.server.command.builder.arguments.minecraft.ArgumentNbtCompoundTag
import net.minestom.server.command.builder.arguments.minecraft.ArgumentResourceLocation
import net.minestom.server.command.builder.ArgumentCallback
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.arguments.Argument.ArgumentMap
import net.minestom.server.command.builder.arguments.Argument.ArgumentFilter
import net.minestom.server.command.builder.arguments.ArgumentEnum
import java.util.function.UnaryOperator
import java.util.Locale
import java.lang.SafeVarargs
import net.minestom.server.command.builder.arguments.ArgumentLoop
import net.minestom.server.command.builder.NodeMaker.ConfiguredNodes
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import net.minestom.server.command.builder.arguments.ArgumentGroup
import net.minestom.server.command.builder.arguments.ArgumentBoolean
import net.minestom.server.command.builder.arguments.number.ArgumentInteger
import net.minestom.server.command.builder.arguments.number.ArgumentDouble
import net.minestom.server.command.builder.arguments.number.ArgumentFloat
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.command.builder.arguments.ArgumentStringArray
import net.minestom.server.command.builder.arguments.ArgumentCommand
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEnchantment
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentPotionEffect
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType
import net.minestom.server.command.builder.arguments.minecraft.ArgumentFloatRange
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2
import net.minestom.server.command.builder.arguments.number.ArgumentLong
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.parser.ValidSyntaxHolder
import net.minestom.server.command.builder.parser.CommandParser
import net.minestom.server.command.builder.CommandResult
import net.minestom.server.command.builder.CommandDispatcher
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.parser.ArgumentParser
import org.jetbrains.annotations.ApiStatus

/**
 * Convenient class listing all the basics [Argument].
 *
 *
 * Please see the specific class documentation for further info.
 */
object ArgumentType {
    /**
     * @see ArgumentLiteral
     */
    @kotlin.jvm.JvmStatic
    fun Literal(id: String): ArgumentLiteral {
        return ArgumentLiteral(id)
    }

    /**
     * @see ArgumentGroup
     */
    fun Group(id: String, vararg arguments: Argument<*>?): ArgumentGroup {
        return ArgumentGroup(id, *arguments)
    }

    /**
     * @see ArgumentLoop
     */
    @SafeVarargs
    fun <T> Loop(id: String, vararg arguments: Argument<T>?): ArgumentLoop<T> {
        return ArgumentLoop(id, *arguments)
    }

    /**
     * @see ArgumentBoolean
     */
    @kotlin.jvm.JvmStatic
    fun Boolean(id: String): ArgumentBoolean {
        return ArgumentBoolean(id)
    }

    /**
     * @see ArgumentInteger
     */
    @kotlin.jvm.JvmStatic
    fun Integer(id: String): ArgumentInteger {
        return ArgumentInteger(id)
    }

    /**
     * @see ArgumentDouble
     */
    fun Double(id: String): ArgumentDouble {
        return ArgumentDouble(id)
    }

    /**
     * @see ArgumentFloat
     */
    @kotlin.jvm.JvmStatic
    fun Float(id: String): ArgumentFloat {
        return ArgumentFloat(id)
    }

    /**
     * @see ArgumentString
     */
    @kotlin.jvm.JvmStatic
    fun String(id: String): ArgumentString {
        return ArgumentString(id)
    }

    /**
     * @see ArgumentWord
     */
    @kotlin.jvm.JvmStatic
    fun Word(id: String): ArgumentWord {
        return ArgumentWord(id)
    }

    /**
     * @see ArgumentStringArray
     */
    @kotlin.jvm.JvmStatic
    fun StringArray(id: String): ArgumentStringArray {
        return ArgumentStringArray(id)
    }

    /**
     * @see ArgumentCommand
     */
    fun Command(id: String): ArgumentCommand {
        return ArgumentCommand(id)
    }

    /**
     * @see ArgumentEnum
     */
    @kotlin.jvm.JvmStatic
    fun <E : Enum<*>?> Enum(id: String, enumClass: Class<E>): ArgumentEnum<E> {
        return ArgumentEnum(id, enumClass)
    }
    // Minecraft specific arguments
    /**
     * @see ArgumentColor
     */
    fun Color(id: String): ArgumentColor {
        return ArgumentColor(id)
    }

    /**
     * @see ArgumentTime
     */
    fun Time(id: String): ArgumentTime {
        return ArgumentTime(id)
    }

    /**
     * @see ArgumentEnchantment
     */
    fun Enchantment(id: String): ArgumentEnchantment {
        return ArgumentEnchantment(id)
    }

    /**
     * @see ArgumentParticle
     */
    fun Particle(id: String): ArgumentParticle {
        return ArgumentParticle(id)
    }

    /**
     * @see ArgumentResourceLocation
     */
    fun ResourceLocation(id: String): ArgumentResourceLocation {
        return ArgumentResourceLocation(id)
    }

    /**
     * @see ArgumentPotionEffect
     */
    @kotlin.jvm.JvmStatic
    fun Potion(id: String): ArgumentPotionEffect {
        return ArgumentPotionEffect(id)
    }

    /**
     * @see ArgumentEntityType
     */
    @kotlin.jvm.JvmStatic
    fun EntityType(id: String): ArgumentEntityType {
        return ArgumentEntityType(id)
    }

    /**
     * @see ArgumentBlockState
     */
    @kotlin.jvm.JvmStatic
    fun BlockState(id: String): ArgumentBlockState {
        return ArgumentBlockState(id)
    }

    /**
     * @see ArgumentIntRange
     */
    fun IntRange(id: String): ArgumentIntRange {
        return ArgumentIntRange(id)
    }

    /**
     * @see ArgumentFloatRange
     */
    fun FloatRange(id: String): ArgumentFloatRange {
        return ArgumentFloatRange(id)
    }

    /**
     * @see ArgumentEntity
     */
    @kotlin.jvm.JvmStatic
    fun Entity(id: String): ArgumentEntity {
        return ArgumentEntity(id)
    }

    /**
     * @see ArgumentItemStack
     */
    @kotlin.jvm.JvmStatic
    fun ItemStack(id: String): ArgumentItemStack {
        return ArgumentItemStack(id)
    }

    /**
     * @see ArgumentComponent
     */
    @kotlin.jvm.JvmStatic
    fun Component(id: String): ArgumentComponent {
        return ArgumentComponent(id)
    }

    /**
     * @see ArgumentUUID
     */
    @kotlin.jvm.JvmStatic
    fun UUID(id: String): ArgumentUUID {
        return ArgumentUUID(id)
    }

    /**
     * @see ArgumentNbtTag
     */
    fun NBT(id: String): ArgumentNbtTag {
        return ArgumentNbtTag(id)
    }

    /**
     * @see ArgumentNbtCompoundTag
     */
    fun NbtCompound(id: String): ArgumentNbtCompoundTag {
        return ArgumentNbtCompoundTag(id)
    }

    /**
     * @see ArgumentRelativeBlockPosition
     */
    @kotlin.jvm.JvmStatic
    fun RelativeBlockPosition(id: String): ArgumentRelativeBlockPosition {
        return ArgumentRelativeBlockPosition(id)
    }

    /**
     * @see ArgumentRelativeVec3
     */
    @kotlin.jvm.JvmStatic
    fun RelativeVec3(id: String): ArgumentRelativeVec3 {
        return ArgumentRelativeVec3(id)
    }

    /**
     * @see ArgumentRelativeVec2
     */
    fun RelativeVec2(id: String): ArgumentRelativeVec2 {
        return ArgumentRelativeVec2(id)
    }

    /**
     * Generates arguments from a string format.
     *
     *
     * Example: "Entity&lt;targets&gt; Integer&lt;number&gt;"
     *
     *
     * Note: this feature is in beta and is very likely to change depending on feedback.
     */
    @kotlin.jvm.JvmStatic
    @ApiStatus.Experimental
    fun generate(format: String): Array<Argument<*>> {
        return ArgumentParser.generate(format)
    }

    /**
     * @see ArgumentLong
     */
    fun Long(id: String): ArgumentLong {
        return ArgumentLong(id)
    }

    /**
     * @see ArgumentEntity
     *
     */
    @Deprecated("use {@link #Entity(String)}")
    fun Entities(id: String): ArgumentEntity {
        return ArgumentEntity(id)
    }
}