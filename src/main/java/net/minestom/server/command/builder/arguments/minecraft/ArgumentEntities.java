package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.entity.Entity;
import net.minestom.server.network.ConnectionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO

/**
 * Represent the target selector argument
 * https://minecraft.gamepedia.com/Commands#Target_selectors
 */
public class ArgumentEntities extends Argument<ArrayList<Entity>> {

    public static final int INVALID_SYNTAX = -2;
    public static final int ONLY_SINGLE_ENTITY_ERROR = -3;
    public static final int ONLY_PLAYERS_ERROR = -4;
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();
    private static final List<String> selectorVariables = Arrays.asList("@p", "@r", "@a", "@e", "@s");
    private static final List<String> playersOnlySelector = Arrays.asList("@p", "@r", "@a", "@s");
    private static final List<String> singleOnlySelector = Arrays.asList("@p", "@r", "@s");
    private static final List<String> validArguments = Arrays.asList("x", "y", "z",
            "distance", "dx", "dy", "dz",
            "scores", "tag", "team", "limit", "sort", "level", "gamemode", "name",
            "x_rotation", "y_rotation", "type", "nbt", "advancements", "predicate");
    private boolean onlySingleEntity;
    private boolean onlyPlayers;

    public ArgumentEntities(String id) {
        super(id, true);
    }

    public ArgumentEntities singleEntity(boolean singleEntity) {
        this.onlySingleEntity = singleEntity;
        return this;
    }

    public ArgumentEntities onlyPlayers(boolean onlyPlayers) {
        this.onlyPlayers = onlyPlayers;
        return this;
    }

    @Override
    public int getCorrectionResult(String value) {
        System.out.println("check: " + value);

        // Check for raw player name
        if (value.length() <= 16) {
            if (CONNECTION_MANAGER.getPlayer(value) != null)
                return SUCCESS;
        }

        // The minimum size is always 0 (for the selector variable, ex: @p)
        if (value.length() < 2)
            return INVALID_SYNTAX;

        // The target selector variable always start by '@'
        if (!value.startsWith("@"))
            return INVALID_SYNTAX;

        final String selectorVariable = value.substring(0, 2);

        // Check if the selector variable used exists
        if (!selectorVariables.contains(selectorVariable))
            return INVALID_SYNTAX;

        // Check if it should only select single entity and if the selector variable valid the condition
        if (onlySingleEntity && !singleOnlySelector.contains(selectorVariable))
            return ONLY_SINGLE_ENTITY_ERROR;

        // Check if it should only select players and if the selector variable valid the condition
        if (onlyPlayers && !playersOnlySelector.contains(selectorVariable))
            return ONLY_PLAYERS_ERROR;

        // The selector is a single selector variable which verify all the conditions
        if (value.length() == 2)
            return SUCCESS;

        // START PARSING THE STRUCTURE
        final String structure = value.substring(2);

        // The structure isn't opened or closed properly
        if (!structure.startsWith("[") || !structure.endsWith("]"))
            return INVALID_SYNTAX;
        final String structureData = structure.substring(1, structure.length() - 1);

        String currentArgument = "";
        for (int i = 0; i < structureData.length(); i++) {
            final char c = structureData.charAt(i);
            if (c == '=') {
                i = retrieveArgument(structureData, currentArgument, i);
            } else {
                currentArgument += c;
            }
        }

        return 0;
    }

    private int retrieveArgument(String structureData, String argument, int index) {
        int finalIndex = index;
        for (int i = index + 1; i < structureData.length(); i++) {
            System.out.println("char: " + structureData.charAt(i));
            System.out.println("retrieve: " + argument);
        }

        return finalIndex;
    }

    @Override
    public ArrayList<Entity> parse(String value) {
        return null;
    }

    @Override
    public int getConditionResult(ArrayList<Entity> value) {
        return SUCCESS;
    }

    public boolean isOnlySingleEntity() {
        return onlySingleEntity;
    }

    public boolean isOnlyPlayers() {
        return onlyPlayers;
    }
}
