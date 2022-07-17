package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Represents the target selector argument.
 * https://minecraft.gamepedia.com/Commands#Target_selectors
 */
public class ArgumentEntity extends Argument<EntityFinder> {

    public static final int INVALID_SYNTAX = -2;
    public static final int ONLY_SINGLE_ENTITY_ERROR = -3;
    public static final int ONLY_PLAYERS_ERROR = -4;
    public static final int INVALID_ARGUMENT_NAME = -5;
    public static final int INVALID_ARGUMENT_VALUE = -6;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{1,16}");
    private static final String SELECTOR_PREFIX = "@";
    private static final List<String> SELECTOR_VARIABLES = Arrays.asList("@p", "@r", "@a", "@e", "@s");
    private static final List<String> PLAYERS_ONLY_SELECTOR = Arrays.asList("@p", "@r", "@a", "@s");
    private static final List<String> SINGLE_ONLY_SELECTOR = Arrays.asList("@p", "@r", "@s");

    private boolean onlySingleEntity;
    private boolean onlyPlayers;

    public ArgumentEntity(String id) {
        super(id);
    }

    @Override
    public @NotNull Result<EntityFinder> parse(CommandReader reader) {
        final String input = reader.readWord();
        // Check for raw player name or UUID
        if (!input.startsWith(SELECTOR_PREFIX)) {

            // Check if the input is a valid UUID
            try {
                final UUID uuid = UUID.fromString(input);
                return Result.success(new EntityFinder()
                        .setTargetSelector(EntityFinder.TargetSelector.MINESTOM_UUID)
                        .setConstantUuid(uuid));
            } catch (IllegalArgumentException ignored) {
            }

            // Check if the input is a valid player name
            if (USERNAME_PATTERN.matcher(input).matches()) {
                return Result.success(new EntityFinder()
                        .setTargetSelector(EntityFinder.TargetSelector.MINESTOM_USERNAME)
                        .setConstantName(input));
            }

            return Result.syntaxError("Input isn't a valid uuid/player name", input, INVALID_ARGUMENT_NAME);
        }

        // The minimum size is always 2 for the selector variable, ex: @p
        if (input.length() < 2)
            return Result.incompatibleType();

        final String selectorVariable = input.substring(0, 2);

        // Check if the selector variable used exists
        if (!SELECTOR_VARIABLES.contains(selectorVariable))
            return Result.syntaxError("Invalid selector variable", input, INVALID_SYNTAX);

        // Check if it should only select single entity and if the selector variable valid the condition
        if (onlySingleEntity && !SINGLE_ONLY_SELECTOR.contains(selectorVariable))
            return Result.syntaxError("Argument requires only a single entity", input, ONLY_SINGLE_ENTITY_ERROR);

        // Check if it should only select players and if the selector variable valid the condition
        if (onlyPlayers && !PLAYERS_ONLY_SELECTOR.contains(selectorVariable))
            return Result.syntaxError("Argument requires only players", input, ONLY_PLAYERS_ERROR);

        // Create the EntityFinder which will be used for the rest of the parsing
        final EntityFinder entityFinder = new EntityFinder()
                .setTargetSelector(toTargetSelector(selectorVariable));

        // The selector is a single selector variable which verify all the conditions
        if (input.length() == 2)
            return Result.success(entityFinder);

        // START PARSING THE STRUCTURE
        return parseStructure(reader, entityFinder, input.substring(2));
    }

    @Contract("_ -> this")
    public ArgumentEntity singleEntity(boolean singleEntity) {
        this.onlySingleEntity = singleEntity;
        return this;
    }

    @Contract("_ -> this")
    public ArgumentEntity onlyPlayers(boolean onlyPlayers) {
        this.onlyPlayers = onlyPlayers;
        return this;
    }

    @Override
    public String parser() {
        return "minecraft:entity";
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return BinaryWriter.makeArray(packetWriter -> {
            byte mask = 0;
            if (this.isOnlySingleEntity()) {
                mask |= 0x01;
            }
            if (this.isOnlyPlayers()) {
                mask |= 0x02;
            }
            packetWriter.writeByte(mask);
        });
    }

    private static @NotNull Result<EntityFinder> parseStructure(@NotNull CommandReader reader,
                                               @NotNull EntityFinder entityFinder,
                                               @NotNull String structure) throws ArgumentSyntaxException {
        // The structure isn't opened
        if (!structure.startsWith("["))
            return Result.syntaxError("Target selector needs to start with brackets", structure, INVALID_SYNTAX);

        // Position cursor to start of structure data
        reader.setCursor(reader.cursor()-(structure.length()-structure.indexOf('[')));

        do {
            final String variable = reader.readUntil('=');
            switch (variable) {
                case "nbt", "name" -> throw new RuntimeException("Unsupported"); //todo parse these
                default -> {
                    final String value = reader.readUntilAny(',', ']');
                    switch (variable) {
                        case "type": {
                            final boolean include = !value.startsWith("!");
                            final String entityName = include ? value : value.substring(1);
                            final EntityType entityType = EntityType.fromNamespaceId(entityName);
                            if (entityType == null)
                                return Result.syntaxError("Invalid entity name", value, INVALID_ARGUMENT_VALUE);
                            entityFinder.setEntity(entityType, include ? EntityFinder.ToggleableType.INCLUDE : EntityFinder.ToggleableType.EXCLUDE);
                            break;
                        }
                        case "gamemode": {
                            final boolean include = !value.startsWith("!");
                            final String gameModeName = include ? value : value.substring(1);
                            try {
                                final GameMode gameMode = GameMode.valueOf(gameModeName.toUpperCase());
                                entityFinder.setGameMode(gameMode, include ? EntityFinder.ToggleableType.INCLUDE : EntityFinder.ToggleableType.EXCLUDE);
                            } catch (IllegalArgumentException e) {
                                return Result.syntaxError("Invalid entity game mode", value, INVALID_ARGUMENT_VALUE);
                            }
                            break;
                        }
                        case "limit":
                            try {
                                int limit = Integer.parseInt(value);
                                if (limit <= 0) {
                                    return Result.syntaxError("Limit must be positive", value, INVALID_ARGUMENT_VALUE);
                                }
                                entityFinder.setLimit(limit);
                            } catch (NumberFormatException e) {
                                return Result.syntaxError("Invalid limit number", value, INVALID_ARGUMENT_VALUE);
                            }
                            break;
                        case "sort":
                            try {
                                EntityFinder.EntitySort entitySort = EntityFinder.EntitySort.valueOf(value.toUpperCase());
                                entityFinder.setEntitySort(entitySort);
                            } catch (IllegalArgumentException e) {
                                return Result.syntaxError("Invalid entity sort", value, INVALID_ARGUMENT_VALUE);
                            }
                            break;
                        case "level":
                            final IntRange level = Argument.parse(new ArgumentIntRange(value)).value();
                            if (level == null)
                                return Result.syntaxError("Invalid level number", value, INVALID_ARGUMENT_VALUE);
                            entityFinder.setLevel(level);
                            break;
                        case "distance":
                            final IntRange distance = Argument.parse(new ArgumentIntRange(value)).value();
                            if (distance == null)
                                return Result.syntaxError("Invalid level number", value, INVALID_ARGUMENT_VALUE);
                            entityFinder.setDistance(distance);
                            break;
                        case "x", "y", "z", "dx", "dy", "dz", "scores", "tag", "team", "x_rotation", "y_rotation", "advancements", "predicate":
                            throw new RuntimeException("Unsupported variable"); //fixme support others too
                        default:
                            return Result.syntaxError("Invalid variable", variable, INVALID_ARGUMENT_NAME);
                    }
                }
            }
        } while (reader.hasRemaining() && reader.getCharAt(reader.cursor()) != ']');

        return Result.success(entityFinder);
    }

    public boolean isOnlySingleEntity() {
        return onlySingleEntity;
    }

    public boolean isOnlyPlayers() {
        return onlyPlayers;
    }

    @Override
    public String toString() {
        if (onlySingleEntity) {
            if (onlyPlayers) {
                return String.format("Player<%s>", getId());
            }
            return String.format("Entity<%s>", getId());
        }
        if (onlyPlayers) {
            return String.format("Players<%s>", getId());
        }
        return String.format("Entities<%s>", getId());
    }

    private static EntityFinder.TargetSelector toTargetSelector(@NotNull String selectorVariable) {
        if (selectorVariable.equals("@p"))
            return EntityFinder.TargetSelector.NEAREST_PLAYER;
        if (selectorVariable.equals("@r"))
            return EntityFinder.TargetSelector.RANDOM_PLAYER;
        if (selectorVariable.equals("@a"))
            return EntityFinder.TargetSelector.ALL_PLAYERS;
        if (selectorVariable.equals("@e"))
            return EntityFinder.TargetSelector.ALL_ENTITIES;
        if (selectorVariable.equals("@s"))
            return EntityFinder.TargetSelector.SELF;
        throw new IllegalStateException("Weird selector variable: " + selectorVariable);
    }
}
