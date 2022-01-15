package net.minestom.server.command.builder.exception;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.command.FixedStringReader;
import net.minestom.server.command.builder.ArgumentCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * This represents a basic command-related exception.
 */
public class CommandException extends RuntimeException {

    /**
     * Represents the length after which characters will be cut off from the result of
     * {@link #generateContextMessage()}. If there are more than CUTOFF_LENGTH characters, the extra characters will be
     * replaced with "...".
     */
    public static final int CUTOFF_LENGTH = 10;

    /**
     * A static translatable component that represents the message that appears after incorrect command syntax, e.g.
     * "/gamemode test<--[HERE]". It's automatically styled to be red and italicised.
     */
    public static final @NotNull Component CONTEXT_HERE = Component.translatable("command.context.here", NamedTextColor.RED, TextDecoration.ITALIC);

    /**
     * This represents the constant that is used for placeholders for error messages. If you want to use {@link
     * String#split(String)} with this, make sure to use {@link #PATTERN_SAFE_PLACEHOLDER}.
     */
    public static final @NotNull String PLACEHOLDER = "%s";

    /**
     * The alternative to {@link #PLACEHOLDER} that is safe to use in regular expressions.
     */
    public static final @NotNull String PATTERN_SAFE_PLACEHOLDER = Pattern.quote(PLACEHOLDER);

    // These are generated with a script from the language files. Try not to modify these.
    public static final @NotNull A0ExceptionGenerator ATTRIBUTE_UNKNOWN = new A0ExceptionGenerator("attribute.unknown", 0, "Unknown attribute");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_UUID_INVALID = new A0ExceptionGenerator("argument.uuid.invalid", 1, "Invalid UUID");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_SELECTOR_NEARESTPLAYER = new A0ExceptionGenerator("argument.entity.selector.nearestPlayer", 2, "Nearest player");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_SELECTOR_RANDOMPLAYER = new A0ExceptionGenerator("argument.entity.selector.randomPlayer", 3, "Random player");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_SELECTOR_ALLPLAYERS = new A0ExceptionGenerator("argument.entity.selector.allPlayers", 4, "All players");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_SELECTOR_ALLENTITIES = new A0ExceptionGenerator("argument.entity.selector.allEntities", 5, "All entities");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_SELECTOR_SELF = new A0ExceptionGenerator("argument.entity.selector.self", 6, "Current entity");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_NAME_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.name.description", 7, "Entity name");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_DISTANCE_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.distance.description", 8, "Distance to entity");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_LEVEL_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.level.description", 9, "Experience level");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_X_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.x.description", 10, "x position");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_Y_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.y.description", 11, "y position");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_Z_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.z.description", 12, "z position");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_DX_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.dx.description", 13, "Entities between x and x + dx");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_DY_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.dy.description", 14, "Entities between y and y + dy");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_DZ_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.dz.description", 15, "Entities between z and z + dz");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_X_ROTATION_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.x_rotation.description", 16, "Entity's x rotation");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_Y_ROTATION_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.y_rotation.description", 17, "Entity's y rotation");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_LIMIT_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.limit.description", 18, "Maximum number of entities to return");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_SORT_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.sort.description", 19, "Sort the entities");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_GAMEMODE_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.gamemode.description", 20, "Players with gamemode");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_TEAM_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.team.description", 21, "Entities on team");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_TYPE_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.type.description", 22, "Entities of type");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_TAG_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.tag.description", 23, "Entities with tag");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_NBT_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.nbt.description", 24, "Entities with NBT");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_SCORES_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.scores.description", 25, "Entities with scores");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_ADVANCEMENTS_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.advancements.description", 26, "Players with advancements");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_PREDICATE_DESCRIPTION = new A0ExceptionGenerator("argument.entity.options.predicate.description", 27, "Custom predicate");
    public static final @NotNull A0ExceptionGenerator COMMAND_FAILED = new A0ExceptionGenerator("command.failed", 28, "An unexpected error occurred trying to execute that command");
    public static final @NotNull A0ExceptionGenerator COMMAND_CONTEXT_HERE = new A0ExceptionGenerator("command.context.here", 29, "<--[HERE]");
    public static final @NotNull A3ExceptionGenerator COMMAND_CONTEXT_PARSE_ERROR = new A3ExceptionGenerator("command.context.parse_error", 30, "%s at position %s: %s");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_RANGE_EMPTY = new A0ExceptionGenerator("argument.range.empty", 31, "Expected value or range of values");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_RANGE_INTS = new A0ExceptionGenerator("argument.range.ints", 32, "Only whole numbers allowed, not decimals");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_RANGE_SWAPPED = new A0ExceptionGenerator("argument.range.swapped", 33, "Min cannot be bigger than max");
    public static final @NotNull A0ExceptionGenerator PERMISSIONS_REQUIRES_PLAYER = new A0ExceptionGenerator("permissions.requires.player", 34, "A player is required to run this command here");
    public static final @NotNull A0ExceptionGenerator PERMISSIONS_REQUIRES_ENTITY = new A0ExceptionGenerator("permissions.requires.entity", 35, "An entity is required to run this command here");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ANGLE_INCOMPLETE = new A0ExceptionGenerator("argument.angle.incomplete", 36, "Incomplete (expected 1 angle)");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ANGLE_INVALID = new A0ExceptionGenerator("argument.angle.invalid", 37, "Invalid angle");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_TOOMANY = new A0ExceptionGenerator("argument.entity.toomany", 38, "Only one entity is allowed, but the provided selector allows more than one");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_PLAYER_TOOMANY = new A0ExceptionGenerator("argument.player.toomany", 39, "Only one player is allowed, but the provided selector allows more than one");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_PLAYER_ENTITIES = new A0ExceptionGenerator("argument.player.entities", 40, "Only players may be affected by this command, but the provided selector includes entities");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_NOTFOUND_ENTITY = new A0ExceptionGenerator("argument.entity.notfound.entity", 41, "No entity was found");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_NOTFOUND_PLAYER = new A0ExceptionGenerator("argument.entity.notfound.player", 42, "No player was found");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_PLAYER_UNKNOWN = new A0ExceptionGenerator("argument.player.unknown", 43, "That player does not exist");
    public static final @NotNull A0ExceptionGenerator ARGUMENTS_NBTPATH_NODE_INVALID = new A0ExceptionGenerator("arguments.nbtpath.node.invalid", 44, "Invalid NBT path element");
    public static final @NotNull A1ExceptionGenerator ARGUMENTS_NBTPATH_NOTHING_FOUND = new A1ExceptionGenerator("arguments.nbtpath.nothing_found", 45, "Found no elements matching %s");
    public static final @NotNull A0ExceptionGenerator ARGUMENTS_OPERATION_INVALID = new A0ExceptionGenerator("arguments.operation.invalid", 46, "Invalid operation");
    public static final @NotNull A0ExceptionGenerator ARGUMENTS_OPERATION_DIV0 = new A0ExceptionGenerator("arguments.operation.div0", 47, "Cannot divide by zero");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_SCOREHOLDER_EMPTY = new A0ExceptionGenerator("argument.scoreHolder.empty", 48, "No relevant score holders could be found");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_BLOCK_TAG_DISALLOWED = new A0ExceptionGenerator("argument.block.tag.disallowed", 49, "Tags aren't allowed here, only actual blocks");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_BLOCK_PROPERTY_UNCLOSED = new A0ExceptionGenerator("argument.block.property.unclosed", 50, "Expected closing ] for block state properties");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_POS_UNLOADED = new A0ExceptionGenerator("argument.pos.unloaded", 51, "That position is not loaded");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_POS_OUTOFWORLD = new A0ExceptionGenerator("argument.pos.outofworld", 52, "That position is out of this world!");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_POS_OUTOFBOUNDS = new A0ExceptionGenerator("argument.pos.outofbounds", 53, "That position is outside the allowed boundaries.");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ROTATION_INCOMPLETE = new A0ExceptionGenerator("argument.rotation.incomplete", 54, "Incomplete (expected 2 coordinates)");
    public static final @NotNull A0ExceptionGenerator ARGUMENTS_SWIZZLE_INVALID = new A0ExceptionGenerator("arguments.swizzle.invalid", 55, "Invalid swizzle, expected combination of 'x', 'y' and 'z'");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_POS2D_INCOMPLETE = new A0ExceptionGenerator("argument.pos2d.incomplete", 56, "Incomplete (expected 2 coordinates)");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_POS3D_INCOMPLETE = new A0ExceptionGenerator("argument.pos3d.incomplete", 57, "Incomplete (expected 3 coordinates)");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_POS_MIXED = new A0ExceptionGenerator("argument.pos.mixed", 58, "Cannot mix world & local coordinates (everything must either use ^ or not)");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_POS_MISSING_DOUBLE = new A0ExceptionGenerator("argument.pos.missing.double", 59, "Expected a coordinate");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_POS_MISSING_INT = new A0ExceptionGenerator("argument.pos.missing.int", 60, "Expected a block position");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ITEM_TAG_DISALLOWED = new A0ExceptionGenerator("argument.item.tag.disallowed", 61, "Tags aren't allowed here, only actual items");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_INVALID = new A0ExceptionGenerator("argument.entity.invalid", 62, "Invalid name or UUID");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_SELECTOR_MISSING = new A0ExceptionGenerator("argument.entity.selector.missing", 63, "Missing selector type");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_SELECTOR_NOT_ALLOWED = new A0ExceptionGenerator("argument.entity.selector.not_allowed", 64, "Selector not allowed");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_UNTERMINATED = new A0ExceptionGenerator("argument.entity.options.unterminated", 65, "Expected end of options");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_DISTANCE_NEGATIVE = new A0ExceptionGenerator("argument.entity.options.distance.negative", 66, "Distance cannot be negative");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_LEVEL_NEGATIVE = new A0ExceptionGenerator("argument.entity.options.level.negative", 67, "Level shouldn't be negative");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_LIMIT_TOOSMALL = new A0ExceptionGenerator("argument.entity.options.limit.toosmall", 68, "Limit must be at least 1");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_NBT_TRAILING = new A0ExceptionGenerator("argument.nbt.trailing", 69, "Unexpected trailing data");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_NBT_EXPECTED_KEY = new A0ExceptionGenerator("argument.nbt.expected.key", 70, "Expected key");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_NBT_EXPECTED_VALUE = new A0ExceptionGenerator("argument.nbt.expected.value", 71, "Expected value");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_ID_INVALID = new A0ExceptionGenerator("argument.id.invalid", 72, "Invalid ID");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_TIME_INVALID_UNIT = new A0ExceptionGenerator("argument.time.invalid_unit", 73, "Invalid unit");
    public static final @NotNull A0ExceptionGenerator ARGUMENT_TIME_INVALID_TICK_COUNT = new A0ExceptionGenerator("argument.time.invalid_tick_count", 74, "Tick count must be non-negative");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_COLOR_INVALID = new A1ExceptionGenerator("argument.color.invalid", 75, "Unknown color '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_DIMENSION_INVALID = new A1ExceptionGenerator("argument.dimension.invalid", 76, "Unknown dimension '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_COMPONENT_INVALID = new A1ExceptionGenerator("argument.component.invalid", 77, "Invalid chat component: %s");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_ANCHOR_INVALID = new A1ExceptionGenerator("argument.anchor.invalid", 78, "Invalid entity anchor position %s");
    public static final @NotNull A1ExceptionGenerator ENCHANTMENT_UNKNOWN = new A1ExceptionGenerator("enchantment.unknown", 79, "Unknown enchantment: %s");
    public static final @NotNull A0ExceptionGenerator LECTERN_TAKE_BOOK = new A0ExceptionGenerator("lectern.take_book", 80, "Take Book");
    public static final @NotNull A1ExceptionGenerator EFFECT_EFFECTNOTFOUND = new A1ExceptionGenerator("effect.effectNotFound", 81, "Unknown effect: %s");
    public static final @NotNull A1ExceptionGenerator ARGUMENTS_OBJECTIVE_NOTFOUND = new A1ExceptionGenerator("arguments.objective.notFound", 82, "Unknown scoreboard objective '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENTS_OBJECTIVE_READONLY = new A1ExceptionGenerator("arguments.objective.readonly", 83, "Scoreboard objective '%s' is read-only");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_CRITERIA_INVALID = new A1ExceptionGenerator("argument.criteria.invalid", 84, "Unknown criterion '%s'");
    public static final @NotNull A1ExceptionGenerator PARTICLE_NOTFOUND = new A1ExceptionGenerator("particle.notFound", 85, "Unknown particle: %s");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_ID_UNKNOWN = new A1ExceptionGenerator("argument.id.unknown", 86, "Unknown ID: %s");
    public static final @NotNull A1ExceptionGenerator ADVANCEMENT_ADVANCEMENTNOTFOUND = new A1ExceptionGenerator("advancement.advancementNotFound", 87, "Unknown advancement: %s");
    public static final @NotNull A1ExceptionGenerator RECIPE_NOTFOUND = new A1ExceptionGenerator("recipe.notFound", 88, "Unknown recipe: %s");
    public static final @NotNull A1ExceptionGenerator ENTITY_NOTFOUND = new A1ExceptionGenerator("entity.notFound", 89, "Unknown entity: %s");
    public static final @NotNull A1ExceptionGenerator PREDICATE_UNKNOWN = new A1ExceptionGenerator("predicate.unknown", 90, "Unknown predicate: %s");
    public static final @NotNull A1ExceptionGenerator ITEM_MODIFIER_UNKNOWN = new A1ExceptionGenerator("item_modifier.unknown", 91, "Unknown item modifier: %s");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_SCOREBOARDDISPLAYSLOT_INVALID = new A1ExceptionGenerator("argument.scoreboardDisplaySlot.invalid", 92, "Unknown display slot '%s'");
    public static final @NotNull A1ExceptionGenerator SLOT_UNKNOWN = new A1ExceptionGenerator("slot.unknown", 93, "Unknown slot '%s'");
    public static final @NotNull A1ExceptionGenerator TEAM_NOTFOUND = new A1ExceptionGenerator("team.notFound", 94, "Unknown team '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENTS_BLOCK_TAG_UNKNOWN = new A1ExceptionGenerator("arguments.block.tag.unknown", 95, "Unknown block tag '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_BLOCK_ID_INVALID = new A1ExceptionGenerator("argument.block.id.invalid", 96, "Unknown block type '%s'");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_BLOCK_PROPERTY_UNKNOWN = new A2ExceptionGenerator("argument.block.property.unknown", 97, "Block %s does not have property '%s'");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_BLOCK_PROPERTY_DUPLICATE = new A2ExceptionGenerator("argument.block.property.duplicate", 98, "Property '%s' can only be set once for block %s");
    public static final @NotNull A3ExceptionGenerator ARGUMENT_BLOCK_PROPERTY_INVALID = new A3ExceptionGenerator("argument.block.property.invalid", 99, "Block %s does not accept '%s' for %s property");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_BLOCK_PROPERTY_NOVALUE = new A2ExceptionGenerator("argument.block.property.novalue", 100, "Expected value for property '%s' on block %s");
    public static final @NotNull A1ExceptionGenerator ARGUMENTS_FUNCTION_TAG_UNKNOWN = new A1ExceptionGenerator("arguments.function.tag.unknown", 101, "Unknown function tag '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENTS_FUNCTION_UNKNOWN = new A1ExceptionGenerator("arguments.function.unknown", 102, "Unknown function %s");
    public static final @NotNull A2ExceptionGenerator ARGUMENTS_ITEM_OVERSTACKED = new A2ExceptionGenerator("arguments.item.overstacked", 103, "%s can only stack up to %s");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_ITEM_ID_INVALID = new A1ExceptionGenerator("argument.item.id.invalid", 104, "Unknown item '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENTS_ITEM_TAG_UNKNOWN = new A1ExceptionGenerator("arguments.item.tag.unknown", 105, "Unknown item tag '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_ENTITY_SELECTOR_UNKNOWN = new A1ExceptionGenerator("argument.entity.selector.unknown", 106, "Unknown selector type '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_VALUELESS = new A1ExceptionGenerator("argument.entity.options.valueless", 107, "Expected value for option '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_UNKNOWN = new A1ExceptionGenerator("argument.entity.options.unknown", 108, "Unknown option '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_INAPPLICABLE = new A1ExceptionGenerator("argument.entity.options.inapplicable", 109, "Option '%s' isn't applicable here");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_SORT_IRREVERSIBLE = new A1ExceptionGenerator("argument.entity.options.sort.irreversible", 110, "Invalid or unknown sort type '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_MODE_INVALID = new A1ExceptionGenerator("argument.entity.options.mode.invalid", 111, "Invalid or unknown game mode '%s'");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_ENTITY_OPTIONS_TYPE_INVALID = new A1ExceptionGenerator("argument.entity.options.type.invalid", 112, "Invalid or unknown entity type '%s'");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_NBT_LIST_MIXED = new A2ExceptionGenerator("argument.nbt.list.mixed", 113, "Can't insert %s into list of %s");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_NBT_ARRAY_MIXED = new A2ExceptionGenerator("argument.nbt.array.mixed", 114, "Can't insert %s into %s");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_NBT_ARRAY_INVALID = new A1ExceptionGenerator("argument.nbt.array.invalid", 115, "Invalid array type '%s'");
    public static final @NotNull A1ExceptionGenerator CLEAR_FAILED_SINGLE = new A1ExceptionGenerator("clear.failed.single", 116, "No items were found on player %s");
    public static final @NotNull A1ExceptionGenerator CLEAR_FAILED_MULTIPLE = new A1ExceptionGenerator("clear.failed.multiple", 117, "No items were found on %s players");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_DOUBLE_LOW = new A2ExceptionGenerator("argument.double.low", 118, "Double must not be less than %s, found %s");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_DOUBLE_BIG = new A2ExceptionGenerator("argument.double.big", 119, "Double must not be more than %s, found %s");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_FLOAT_LOW = new A2ExceptionGenerator("argument.float.low", 120, "Float must not be less than %s, found %s");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_FLOAT_BIG = new A2ExceptionGenerator("argument.float.big", 121, "Float must not be more than %s, found %s");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_INTEGER_LOW = new A2ExceptionGenerator("argument.integer.low", 122, "Integer must not be less than %s, found %s");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_INTEGER_BIG = new A2ExceptionGenerator("argument.integer.big", 123, "Integer must not be more than %s, found %s");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_LONG_LOW = new A2ExceptionGenerator("argument.long.low", 124, "Long must not be less than %s, found %s");
    public static final @NotNull A2ExceptionGenerator ARGUMENT_LONG_BIG = new A2ExceptionGenerator("argument.long.big", 125, "Long must not be more than %s, found %s");
    public static final @NotNull A1ExceptionGenerator ARGUMENT_LITERAL_INCORRECT = new A1ExceptionGenerator("argument.literal.incorrect", 126, "Expected literal %s");
    public static final @NotNull A0ExceptionGenerator PARSING_QUOTE_EXPECTED_START = new A0ExceptionGenerator("parsing.quote.expected.start", 127, "Expected quote to start a string");
    public static final @NotNull A0ExceptionGenerator PARSING_QUOTE_EXPECTED_END = new A0ExceptionGenerator("parsing.quote.expected.end", 128, "Unclosed quoted string");
    public static final @NotNull A1ExceptionGenerator PARSING_QUOTE_ESCAPE = new A1ExceptionGenerator("parsing.quote.escape", 129, "Invalid escape sequence '\\%s' in quoted string");
    public static final @NotNull A1ExceptionGenerator PARSING_BOOL_INVALID = new A1ExceptionGenerator("parsing.bool.invalid", 130, "Invalid boolean, expected 'true' or 'false' but found '%s'");
    public static final @NotNull A1ExceptionGenerator PARSING_INT_INVALID = new A1ExceptionGenerator("parsing.int.invalid", 131, "Invalid integer '%s'");
    public static final @NotNull A0ExceptionGenerator PARSING_INT_EXPECTED = new A0ExceptionGenerator("parsing.int.expected", 132, "Expected integer");
    public static final @NotNull A1ExceptionGenerator PARSING_LONG_INVALID = new A1ExceptionGenerator("parsing.long.invalid", 133, "Invalid long '%s'");
    public static final @NotNull A0ExceptionGenerator PARSING_LONG_EXPECTED = new A0ExceptionGenerator("parsing.long.expected", 134, "Expected long");
    public static final @NotNull A1ExceptionGenerator COMMAND_EXCEPTION = new A1ExceptionGenerator("command.exception", 135, "Could not parse command: %s");
    public static final @NotNull A1ExceptionGenerator PARSING_DOUBLE_INVALID = new A1ExceptionGenerator("parsing.double.invalid", 136, "Invalid double '%s'");
    public static final @NotNull A0ExceptionGenerator PARSING_DOUBLE_EXPECTED = new A0ExceptionGenerator("parsing.double.expected", 137, "Expected double");
    public static final @NotNull A1ExceptionGenerator PARSING_FLOAT_INVALID = new A1ExceptionGenerator("parsing.float.invalid", 138, "Invalid float '%s'");
    public static final @NotNull A0ExceptionGenerator PARSING_FLOAT_EXPECTED = new A0ExceptionGenerator("parsing.float.expected", 139, "Expected float");
    public static final @NotNull A0ExceptionGenerator PARSING_BOOL_EXPECTED = new A0ExceptionGenerator("parsing.bool.expected", 140, "Expected boolean");
    public static final @NotNull A1ExceptionGenerator PARSING_EXPECTED = new A1ExceptionGenerator("parsing.expected", 141, "Expected '%s'");
    public static final @NotNull A0ExceptionGenerator COMMAND_UNKNOWN_COMMAND = new A0ExceptionGenerator("command.unknown.command", 142, "Unknown or incomplete command, see below for error");
    public static final @NotNull A0ExceptionGenerator COMMAND_UNKNOWN_ARGUMENT = new A0ExceptionGenerator("command.unknown.argument", 143, "Incorrect argument for command");
    public static final @NotNull A0ExceptionGenerator COMMAND_EXPECTED_SEPARATOR = new A0ExceptionGenerator("command.expected.separator", 144, "Expected whitespace to end one argument, but found trailing data");

    private final @NotNull String text;
    private final int position;
    private final int errorCode;
    private final @Nullable Component component;

    /**
     * Creates a new CommandException. The cause, error message, and component are all assumed to be null.
     * @param text the text or command that caused this exception
     * @param position the position in the {@code text} parameter that has been read to
     * @param errorCode the error code that this exception is
     */
    public CommandException(@NotNull String text, int position, int errorCode) {
        this(text, position, errorCode, null, null, null);
    }

    /**
     * Creates a new CommandException. The cause is assumed to be null and the component is set to
     * {@code Component.text(errorMessage)} or null if the message is null.
     * @param text the text or command that caused this exception
     * @param position the position in the {@code text} parameter that has been read to
     * @param errorCode the error code that this exception is
     */
    public CommandException(@NotNull String text, int position, int errorCode, @Nullable String errorMessage) {
        this(text, position, errorCode, errorMessage == null ? null : Component.text(errorCode), errorMessage, null);
    }

    /**
     * Creates a new CommandException. The cause is assumed to be null and the error message is set to the plaintext
     * serialization of the provided component, or null if the component is null.
     * @param text the text or command that caused this exception
     * @param position the position in the {@code text} parameter that has been read to
     * @param errorCode the error code that this exception is
     * @param component the component that should be displayed to players
     */
    public CommandException(@NotNull String text, int position, int errorCode, @Nullable Component component) {
        this(text, position, errorCode, component, component == null ? null : PlainTextComponentSerializer.plainText().serialize(component), null);
    }

    /**
     * Creates a new CommandException. The cause is assumed to be null.
     * @param text the text or command that caused this exception
     * @param position the position in the {@code text} parameter that has been read to
     * @param errorCode the error code that this exception is
     * @param component the component that should be displayed to players
     * @param errorMessage the error message that should be displayed
     */
    public CommandException(@NotNull String text, int position, int errorCode, @Nullable Component component,
                            @NotNull String errorMessage) {
        this(text, position, errorCode, component, errorMessage, null);
    }

    /**
     * Creates a new CommandException.
     * @param text the text or command that caused this exception
     * @param position the position in the {@code text} parameter that has been read to
     * @param errorCode the error code that this exception is
     * @param component the component that should be displayed to players
     * @param errorMessage the error message that should be displayed
     * @param cause the throwable that was the cause of this exception
     */
    public CommandException(@NotNull String text, int position, int errorCode, @Nullable Component component,
                            @Nullable String errorMessage, @Nullable Throwable cause) {
        super(errorMessage, cause);
        this.text = text;
        this.position = position;
        this.errorCode = errorCode;
        this.component = component;
    }

    /**
     * @return the string that this exception was created from
     */
    public @NotNull String getText() {
        return text;
    }

    /**
     * @return the position in the text that has been read to
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return this exception's error code
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @return the component that this exception contains (may be null)
     */
    public @Nullable Component getComponent() {
        return component;
    }

    /**
     * @return the component that should be displayed, or {@code CommandException.COMMAND_UNKNOWN_COMMAND.generateComponent()}
     * if it is null.
     */
    public @NotNull Component getDisplayComponent(){
        return component == null ? CommandException.COMMAND_UNKNOWN_COMMAND.generateComponent() : component;
    }

    /**
     * Generates an error message that displays where the error occurred.
     * @see FixedStringReader#generateContextMessage(String, int)
     */
    public @NotNull Component generateContextMessage() {
        return FixedStringReader.generateContextMessage(text, position);
    }

    /**
     * The standard callback, which sends the sender messages generated from the exception.
     */
    public static @NotNull ArgumentCallback STANDARD_CALLBACK = (origin, exception) -> {
        origin.sender().sendMessage(exception.getDisplayComponent());
        origin.sender().sendMessage(exception.generateContextMessage());
    };
}
