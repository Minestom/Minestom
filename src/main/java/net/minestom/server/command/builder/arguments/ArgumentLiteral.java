package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgumentLiteral extends Argument<String> {

    public static final int INVALID_VALUE_ERROR = 1;

    private final String[] aliases;
    private final Set<String> names;

    public ArgumentLiteral(@NotNull String id, @NotNull String @NotNull ... aliases) {
        super(id);
        this.aliases = aliases;
        this.names = Stream.concat(Stream.of(id), Arrays.stream(aliases)).collect(Collectors.toSet());
    }

    /**
     * Gets aliases of the argument.
     *
     * @return the argument
     */
    public @NotNull String @NotNull [] getAliases() {
        return this.aliases;
    }

    /**
     * Gets names of the argument.
     *
     * @return the argument
     */
    public @NotNull Set<String> getNames() {
        return this.names;
    }

    @NotNull
    @Override
    public String parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        if (names.stream().noneMatch(name -> name.equals(input)))
            throw new ArgumentSyntaxException("Invalid literal value", input, INVALID_VALUE_ERROR);

        return input;
    }

    @Override
    public String parser() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("Literal<%s>", getId());
    }
}
