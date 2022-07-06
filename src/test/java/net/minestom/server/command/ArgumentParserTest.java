package net.minestom.server.command;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.parser.ArgumentParser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Test string version of arguments.
 */
public class ArgumentParserTest {

    @Test
    public void testArgumentParser() {
        // Test each argument
        assertParserEquals("Literal<example>", ArgumentType.Literal("example"));
        assertParserEquals("Boolean<example>", ArgumentType.Boolean("example"));
        assertParserEquals("Integer<example>", ArgumentType.Integer("example"));
        assertParserEquals("Double<example>", ArgumentType.Double("example"));
        assertParserEquals("Float<example>", ArgumentType.Float("example"));
        assertParserEquals("String<example>", ArgumentType.String("example"));
        assertParserEquals("Word<example>", ArgumentType.Word("example"));
        assertParserEquals("StringArray<example>", ArgumentType.StringArray("example"));
        assertParserEquals("Command<example>", ArgumentType.Command("example"));
        assertParserEquals("Color<example>", ArgumentType.Color("example"));
        assertParserEquals("Time<example>", ArgumentType.Time("example"));
        assertParserEquals("Enchantment<example>", ArgumentType.Enchantment("example"));
        assertParserEquals("Particle<example>", ArgumentType.Particle("example"));
        assertParserEquals("ResourceLocation<example>", ArgumentType.ResourceLocation("example"));
        assertParserEquals("Potion<example>", ArgumentType.Potion("example"));
        assertParserEquals("EntityType<example>", ArgumentType.EntityType("example"));
        assertParserEquals("BlockState<example>", ArgumentType.BlockState("example"));
        assertParserEquals("IntRange<example>", ArgumentType.IntRange("example"));
        assertParserEquals("FloatRange<example>", ArgumentType.FloatRange("example"));
        assertParserEquals("ItemStack<example>", ArgumentType.ItemStack("example"));
        assertParserEquals("Component<example>", ArgumentType.Component("example"));
        assertParserEquals("UUID<example>", ArgumentType.UUID("example"));
        assertParserEquals("NBT<example>", ArgumentType.NBT("example"));
        assertParserEquals("NBTCompound<example>", ArgumentType.NbtCompound("example"));
        assertParserEquals("RelativeBlockPosition<example>", ArgumentType.RelativeBlockPosition("example"));
        assertParserEquals("RelativeVec2<example>", ArgumentType.RelativeVec2("example"));
        assertParserEquals("RelativeVec3<example>", ArgumentType.RelativeVec3("example"));
        assertParserEquals("Entities<example>", ArgumentType.Entity("example"));
        assertParserEquals("Entity<example>", ArgumentType.Entity("example").singleEntity(true));
        assertParserEquals("Players<example>", ArgumentType.Entity("example").onlyPlayers(true));
        assertParserEquals("Player<example>", ArgumentType.Entity("example").onlyPlayers(true).singleEntity(true));

        // Test multiple argument functionality
        assertParserEquals("NBT<arg1> RelativeVec2<arg2>", ArgumentType.NBT("arg1"), ArgumentType.RelativeVec2("arg2"));
        assertParserEquals("Word<arg1> UUID<arg2> NBT<arg3>", ArgumentType.Word("arg1"), ArgumentType.UUID("arg2"), ArgumentType.NBT("arg3"));
    }

    private static void assertParserEquals(@NotNull String input, @NotNull Argument<?> @NotNull ... args) {
        assertArrayEquals(ArgumentParser.generate(input), args);
    }
}
