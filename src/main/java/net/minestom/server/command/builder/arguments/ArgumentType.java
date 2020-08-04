package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.arguments.minecraft.*;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEnchantment;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentPotion;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;

/**
 * Class listing all the basic arguments
 */
public class ArgumentType {

    public static ArgumentBoolean Boolean(String id) {
        return new ArgumentBoolean(id);
    }

    public static ArgumentLong Long(String id) {
        return new ArgumentLong(id);
    }

    public static ArgumentInteger Integer(String id) {
        return new ArgumentInteger(id);
    }

    public static ArgumentDouble Double(String id) {
        return new ArgumentDouble(id);
    }

    public static ArgumentFloat Float(String id) {
        return new ArgumentFloat(id);
    }

    public static ArgumentString String(String id) {
        return new ArgumentString(id);
    }

    public static ArgumentWord Word(String id) {
        return new ArgumentWord(id);
    }

    public static ArgumentDynamicWord DynamicWord(String id) {
        return new ArgumentDynamicWord(id);
    }

    public static ArgumentStringArray StringArray(String id) {
        return new ArgumentStringArray(id);
    }

    public static ArgumentDynamicStringArray DynamicStringArray(String id) {
        return new ArgumentDynamicStringArray(id);
    }

    // Minecraft specific

    public static ArgumentColor Color(String id) {
        return new ArgumentColor(id);
    }

    public static ArgumentTime Time(String id) {
        return new ArgumentTime(id);
    }

    public static ArgumentEnchantment Enchantment(String id) {
        return new ArgumentEnchantment(id);
    }

    public static ArgumentParticle Particle(String id) {
        return new ArgumentParticle(id);
    }

    public static ArgumentPotion Potion(String id) {
        return new ArgumentPotion(id);
    }

    public static ArgumentEntityType EntityType(String id) {
        return new ArgumentEntityType(id);
    }

    public static ArgumentIntRange IntRange(String id) {
        return new ArgumentIntRange(id);
    }

    public static ArgumentFloatRange FloatRange(String id) {
        return new ArgumentFloatRange(id);
    }

    public static ArgumentEntities Entities(String id) {
        return new ArgumentEntities(id);
    }

}
