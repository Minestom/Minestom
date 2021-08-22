package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.NotNull;

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
    // List with all the valid arguments
    private static final List<String> VALID_ARGUMENTS = Arrays.asList(
            "x", "y", "z",
            "distance", "dx", "dy", "dz",
            "scores", "tag", "team", "limit", "sort", "level", "gamemode", "name",
            "x_rotation", "y_rotation", "type", "nbt", "advancements", "predicate");

    // List with all the easily parsable arguments which only require reading until a specific character (comma)
    private static final List<String> SIMPLE_ARGUMENTS = Arrays.asList(
            "x", "y", "z",
            "distance", "dx", "dy", "dz",
            "scores", "tag", "team", "limit", "sort", "level", "gamemode",
            "x_rotation", "y_rotation", "type", "advancements", "predicate");

    private boolean onlySingleEntity;
    private boolean onlyPlayers;

    public ArgumentEntity(String id) {
        super(id, true);
    }

    public ArgumentEntity singleEntity(boolean singleEntity) {
        this.onlySingleEntity = singleEntity;
        return this;
    }

    public ArgumentEntity onlyPlayers(boolean onlyPlayers) {
        this.onlyPlayers = onlyPlayers;
        return this;
    }

    @NotNull
    @Override
    public EntityFinder parse(@NotNull String input) throws ArgumentSyntaxException {
        return staticParse(input, onlySingleEntity, onlyPlayers);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:entity";
        argumentNode.properties = BinaryWriter.makeArray(packetWriter -> {
            byte mask = 0;
            if (this.isOnlySingleEntity()) {
                mask |= 0x01;
            }
            if (this.isOnlyPlayers()) {
                mask |= 0x02;
            }
            packetWriter.writeByte(mask);
        });

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    /**
     * @deprecated use {@link Argument#parse(Argument)}
     */
    @Deprecated
    @NotNull
    public static EntityFinder staticParse(@NotNull String input,
                                           boolean onlySingleEntity, boolean onlyPlayers) throws ArgumentSyntaxException {
        // Check for raw player name or UUID
        if (!input.contains(SELECTOR_PREFIX) && !input.contains(StringUtils.SPACE)) {

            // Check if the input is a valid UUID
            try {
                final UUID uuid = UUID.fromString(input);
                return new EntityFinder()
                        .setTargetSelector(EntityFinder.TargetSelector.MINESTOM_UUID)
                        .setConstantUuid(uuid);
            } catch (IllegalArgumentException ignored) {
            }

            // Check if the input is a valid player name
            if (USERNAME_PATTERN.matcher(input).matches()) {
                return new EntityFinder()
                        .setTargetSelector(EntityFinder.TargetSelector.MINESTOM_USERNAME)
                        .setConstantName(input);
            }
        }

        // The minimum size is always 2 (for the selector variable, ex: @p)
        if (input.length() < 2)
            throw new ArgumentSyntaxException("Length needs to be > 1", input, INVALID_SYNTAX);

        // The target selector variable always start by '@'
        if (!input.startsWith(SELECTOR_PREFIX))
            throw new ArgumentSyntaxException("Target selector needs to start with @", input, INVALID_SYNTAX);

        final String selectorVariable = input.substring(0, 2);

        // Check if the selector variable used exists
        if (!SELECTOR_VARIABLES.contains(selectorVariable))
            throw new ArgumentSyntaxException("Invalid selector variable", input, INVALID_SYNTAX);

        // Check if it should only select single entity and if the selector variable valid the condition
        if (onlySingleEntity && !SINGLE_ONLY_SELECTOR.contains(selectorVariable))
            throw new ArgumentSyntaxException("Argument requires only a single entity", input, ONLY_SINGLE_ENTITY_ERROR);

        // Check if it should only select players and if the selector variable valid the condition
        if (onlyPlayers && !PLAYERS_ONLY_SELECTOR.contains(selectorVariable))
            throw new ArgumentSyntaxException("Argument requires only players", input, ONLY_PLAYERS_ERROR);

        // Create the EntityFinder which will be used for the rest of the parsing
        final EntityFinder entityFinder = new EntityFinder()
                .setTargetSelector(toTargetSelector(selectorVariable));

        // The selector is a single selector variable which verify all the conditions
        if (input.length() == 2)
            return entityFinder;

        // START PARSING THE STRUCTURE
        final String structure = input.substring(2);
        return parseStructure(input, entityFinder, structure);
    }

    @NotNull
    private static EntityFinder parseStructure(@NotNull String input,
                                               @NotNull EntityFinder entityFinder,
                                               @NotNull String structure) throws ArgumentSyntaxException {
        // The structure isn't opened or closed properly
        if (!structure.startsWith("[") || !structure.endsWith("]"))
            throw new ArgumentSyntaxException("Target selector needs to start and end with brackets", input, INVALID_SYNTAX);

        // Remove brackets
        final String structureData = structure.substring(1, structure.length() - 1);
        //System.out.println("structure data: " + structureData);

        String currentArgument = "";
        for (int i = 0; i < structureData.length(); i++) {
            final char c = structureData.charAt(i);
            if (c == '=') {

                // Replace all unnecessary spaces
                currentArgument = currentArgument.trim();

                if (!VALID_ARGUMENTS.contains(currentArgument))
                    throw new ArgumentSyntaxException("Argument name '" + currentArgument + "' does not exist", input, INVALID_ARGUMENT_NAME);

                i = parseArgument(entityFinder, currentArgument, input, structureData, i);
                currentArgument = ""; // Reset current argument
            } else {
                currentArgument += c;
            }
        }

        return entityFinder;
    }

    private static int parseArgument(@NotNull EntityFinder entityFinder,
                                     @NotNull String argumentName,
                                     @NotNull String input,
                                     @NotNull String structureData, int beginIndex) throws ArgumentSyntaxException {
        final char comma = ',';
        final boolean isSimple = SIMPLE_ARGUMENTS.contains(argumentName);

        int finalIndex = beginIndex + 1;
        StringBuilder valueBuilder = new StringBuilder();
        for (; finalIndex < structureData.length(); finalIndex++) {
            final char c = structureData.charAt(finalIndex);

            // Command is parsed
            if (isSimple && c == comma)
                break;

            valueBuilder.append(c);
        }

        final String value = valueBuilder.toString().trim();

        //System.out.println("value: " + value);
        switch (argumentName) {
            case "type": {
                final boolean include = !value.startsWith("!");
                final String entityName = include ? value : value.substring(1);
                final EntityType entityType = EntityType.fromNamespaceId(entityName);
                if (entityType == null)
                    throw new ArgumentSyntaxException("Invalid entity name", input, INVALID_ARGUMENT_VALUE);
                entityFinder.setEntity(entityType, include ? EntityFinder.ToggleableType.INCLUDE : EntityFinder.ToggleableType.EXCLUDE);
                break;
            }
            case "gamemode": {
                final boolean include = !value.startsWith("!");
                final String gameModeName = include ? value : value.substring(1);
                try {
                    final GameMode gameMode = GameMode.valueOf(gameModeName);
                    entityFinder.setGameMode(gameMode, include ? EntityFinder.ToggleableType.INCLUDE : EntityFinder.ToggleableType.EXCLUDE);
                } catch (IllegalArgumentException e) {
                    throw new ArgumentSyntaxException("Invalid entity game mode", input, INVALID_ARGUMENT_VALUE);
                }
                break;
            }
            case "limit":
                try {
                    final int limit = Integer.parseInt(value);
                    entityFinder.setLimit(limit);
                } catch (NumberFormatException e) {
                    throw new ArgumentSyntaxException("Invalid limit number", input, INVALID_ARGUMENT_VALUE);
                }
                break;
            case "sort":
                try {
                    EntityFinder.EntitySort entitySort = EntityFinder.EntitySort.valueOf(value.toUpperCase());
                    entityFinder.setEntitySort(entitySort);
                } catch (IllegalArgumentException e) {
                    throw new ArgumentSyntaxException("Invalid entity sort", input, INVALID_ARGUMENT_VALUE);
                }
                break;
            case "level":
                try {
                    final IntRange level = Argument.parse(new ArgumentIntRange(value));
                    entityFinder.setLevel(level);
                } catch (ArgumentSyntaxException e) {
                    throw new ArgumentSyntaxException("Invalid level number", input, INVALID_ARGUMENT_VALUE);
                }
                break;
            case "distance":
                try {
                    final IntRange distance = Argument.parse(new ArgumentIntRange(value));
                    entityFinder.setDistance(distance);
                } catch (ArgumentSyntaxException e) {
                    throw new ArgumentSyntaxException("Invalid level number", input, INVALID_ARGUMENT_VALUE);
                }
                break;
        }

        return finalIndex;
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
