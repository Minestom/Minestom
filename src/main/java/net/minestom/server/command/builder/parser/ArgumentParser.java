package net.minestom.server.command.builder.parser;

import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.command.builder.arguments.minecraft.*;
import net.minestom.server.command.builder.arguments.minecraft.registry.*;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ArgumentParser {

    private static final Map<String, Function<String, Argument<?>>> ARGUMENT_FUNCTION_MAP = new ConcurrentHashMap<>();

    static {
        ARGUMENT_FUNCTION_MAP.put("literal", ArgumentLiteral::new);
        ARGUMENT_FUNCTION_MAP.put("boolean", ArgumentBoolean::new);
        ARGUMENT_FUNCTION_MAP.put("integer", ArgumentInteger::new);
        ARGUMENT_FUNCTION_MAP.put("double", ArgumentDouble::new);
        ARGUMENT_FUNCTION_MAP.put("float", ArgumentFloat::new);
        ARGUMENT_FUNCTION_MAP.put("string", ArgumentString::new);
        ARGUMENT_FUNCTION_MAP.put("word", ArgumentWord::new);
        ARGUMENT_FUNCTION_MAP.put("stringarray", ArgumentStringArray::new);
        ARGUMENT_FUNCTION_MAP.put("command", ArgumentCommand::new);
        // TODO enum
        ARGUMENT_FUNCTION_MAP.put("color", ArgumentColor::new);
        ARGUMENT_FUNCTION_MAP.put("time", ArgumentTime::new);
        ARGUMENT_FUNCTION_MAP.put("enchantment", ArgumentEnchantment::new);
        ARGUMENT_FUNCTION_MAP.put("particle", ArgumentParticle::new);
        ARGUMENT_FUNCTION_MAP.put("resourceLocation", ArgumentResourceLocation::new);
        ARGUMENT_FUNCTION_MAP.put("potion", ArgumentPotionEffect::new);
        ARGUMENT_FUNCTION_MAP.put("entityType", ArgumentEntityType::new);
        ARGUMENT_FUNCTION_MAP.put("blockState", ArgumentBlockState::new);
        ARGUMENT_FUNCTION_MAP.put("intrange", ArgumentIntRange::new);
        ARGUMENT_FUNCTION_MAP.put("floatrange", ArgumentFloatRange::new);

        ARGUMENT_FUNCTION_MAP.put("entity", s -> new ArgumentEntity(s).singleEntity(true));
        ARGUMENT_FUNCTION_MAP.put("entities", ArgumentEntity::new);
        ARGUMENT_FUNCTION_MAP.put("player", s -> new ArgumentEntity(s).singleEntity(true).onlyPlayers(true));
        ARGUMENT_FUNCTION_MAP.put("players", s -> new ArgumentEntity(s).onlyPlayers(true));

        ARGUMENT_FUNCTION_MAP.put("itemstack", ArgumentItemStack::new);
        ARGUMENT_FUNCTION_MAP.put("component", ArgumentComponent::new);
        ARGUMENT_FUNCTION_MAP.put("uuid", ArgumentUUID::new);
        ARGUMENT_FUNCTION_MAP.put("nbt", ArgumentNbtTag::new);
        ARGUMENT_FUNCTION_MAP.put("nbtcompound", ArgumentNbtCompoundTag::new);
        ARGUMENT_FUNCTION_MAP.put("relativeblockposition", ArgumentRelativeBlockPosition::new);
        ARGUMENT_FUNCTION_MAP.put("relativevec3", ArgumentRelativeVec3::new);
        ARGUMENT_FUNCTION_MAP.put("relativevec2", ArgumentRelativeVec2::new);
    }

    @ApiStatus.Experimental
    public static @NotNull Argument<?>[] generate(@NotNull String format) {
        List<Argument<?>> result = new ArrayList<>();

        // 0 = no state
        // 1 = inside angle bracket <>
        int state = 0;
        // function to create an argument from its identifier
        // not null during state 1
        Function<String, Argument<?>> argumentFunction = null;

        StringBuilder builder = new StringBuilder();

        // test: Integer<name> String<hey>
        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);

            // No state
            if (state == 0) {
                if (c == ' ') {
                    // Use literal as the default argument
                    final String argument = builder.toString();
                    if (argument.length() != 0) {
                        result.add(new ArgumentLiteral(argument));
                        builder = new StringBuilder();
                    }
                } else if (c == '<') {
                    // Retrieve argument type
                    final String argument = builder.toString();
                    argumentFunction = ARGUMENT_FUNCTION_MAP.get(argument.toLowerCase(Locale.ROOT));
                    if (argumentFunction == null) {
                        throw new IllegalArgumentException("error invalid argument name: " + argument);
                    }

                    builder = new StringBuilder();
                    state = 1;
                } else {
                    // Append to builder
                    builder.append(c);
                }

                continue;
            }

            // Inside bracket <>
            if (state == 1) {
                if (c == '>') {
                    final String param = builder.toString();
                    // TODO argument options
                    Argument<?> argument = argumentFunction.apply(param);
                    result.add(argument);

                    builder = new StringBuilder();
                    state = 0;
                } else {
                    builder.append(c);
                }

                continue;
            }

        }

        // Use remaining as literal if present
        if (state == 0) {
            final String argument = builder.toString();
            if (argument.length() != 0) {
                result.add(new ArgumentLiteral(argument));
            }
        }

        return result.toArray(Argument[]::new);
    }

    @Nullable
    public static ArgumentResult validate(@NotNull Argument<?> argument,
                                          @NotNull Argument<?>[] arguments, int argIndex,
                                          @NotNull String[] inputArguments, int inputIndex) {
        final boolean end = inputIndex == inputArguments.length;
        if (end) // Stop if there is no input to analyze left
            return null;

        // the parsed argument value, null if incorrect
        Object parsedValue = null;
        // the argument exception, null if the input is correct
        ArgumentSyntaxException argumentSyntaxException = null;
        // true if the arg is valid, false otherwise
        boolean correct = false;
        // The raw string value of the argument
        String rawArg = null;

        if (argument.useRemaining()) {
            final boolean hasArgs = inputArguments.length > inputIndex;
            // Verify if there is any string part available
            if (hasArgs) {
                StringBuilder builder = new StringBuilder();
                // Argument is supposed to take the rest of the command input
                for (int i = inputIndex; i < inputArguments.length; i++) {
                    final String arg = inputArguments[i];
                    if (builder.length() > 0)
                        builder.append(StringUtils.SPACE);
                    builder.append(arg);
                }

                rawArg = builder.toString();

                try {
                    parsedValue = argument.parse(rawArg);
                    correct = true;
                } catch (ArgumentSyntaxException exception) {
                    argumentSyntaxException = exception;
                }
            }
        } else {
            // Argument is either single-word or can accept optional delimited space(s)
            StringBuilder builder = new StringBuilder();
            for (int i = inputIndex; i < inputArguments.length; i++) {
                builder.append(inputArguments[i]);

                rawArg = builder.toString();

                try {
                    parsedValue = argument.parse(rawArg);

                    // Prevent quitting the parsing too soon if the argument
                    // does not allow space
                    final boolean lastArgumentIteration = argIndex + 1 == arguments.length;
                    if (lastArgumentIteration && i + 1 < inputArguments.length) {
                        if (!argument.allowSpace())
                            break;
                        builder.append(StringUtils.SPACE);
                        continue;
                    }

                    correct = true;

                    inputIndex = i + 1;
                    break;
                } catch (ArgumentSyntaxException exception) {
                    argumentSyntaxException = exception;

                    if (!argument.allowSpace()) {
                        // rawArg should be the remaining
                        for (int j = i + 1; j < inputArguments.length; j++) {
                            final String arg = inputArguments[j];
                            if (builder.length() > 0)
                                builder.append(StringUtils.SPACE);
                            builder.append(arg);
                        }
                        rawArg = builder.toString();
                        break;
                    }
                    builder.append(StringUtils.SPACE);
                }
            }
        }

        ArgumentResult argumentResult = new ArgumentResult();
        argumentResult.argument = argument;
        argumentResult.correct = correct;
        argumentResult.inputIndex = inputIndex;
        argumentResult.argumentSyntaxException = argumentSyntaxException;

        argumentResult.useRemaining = argument.useRemaining();

        argumentResult.rawArg = rawArg;

        argumentResult.parsedValue = parsedValue;
        return argumentResult;
    }

    public static class ArgumentResult {
        public Argument<?> argument;
        public boolean correct;
        public int inputIndex;
        public ArgumentSyntaxException argumentSyntaxException;

        public boolean useRemaining;

        public String rawArg;

        // If correct
        public Object parsedValue;
    }

}
