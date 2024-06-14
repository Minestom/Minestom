package net.minestom.server.network.packet.server;

import java.util.concurrent.atomic.AtomicInteger;

public final class ServerPacketIdentifier {
    private static final AtomicInteger STATUS_ID = new AtomicInteger(0);
    private static final AtomicInteger LOGIN_ID = new AtomicInteger(0);
    private static final AtomicInteger CONFIGURATION_ID = new AtomicInteger(0);
    private static final AtomicInteger PLAY_ID = new AtomicInteger(0);

    public static final int STATUS_RESPONSE = nextStatusId();
    public static final int STATUS_PING_RESPONSE = nextStatusId();

    public static final int LOGIN_DISCONNECT = nextLoginId();
    public static final int LOGIN_ENCRYPTION_REQUEST = nextLoginId();
    public static final int LOGIN_SUCCESS = nextLoginId();
    public static final int LOGIN_SET_COMPRESSION = nextLoginId();
    public static final int LOGIN_PLUGIN_REQUEST = nextLoginId();
    public static final int LOGIN_COOKIE_REQUEST = nextLoginId();

    public static final int CONFIGURATION_COOKIE_REQUEST = nextConfigurationId();
    public static final int CONFIGURATION_PLUGIN_MESSAGE = nextConfigurationId();
    public static final int CONFIGURATION_DISCONNECT = nextConfigurationId();
    public static final int CONFIGURATION_FINISH_CONFIGURATION = nextConfigurationId();
    public static final int CONFIGURATION_KEEP_ALIVE = nextConfigurationId();
    public static final int CONFIGURATION_PING = nextConfigurationId();
    public static final int CONFIGURATION_RESET_CHAT = nextConfigurationId();
    public static final int CONFIGURATION_REGISTRY_DATA = nextConfigurationId();
    public static final int CONFIGURATION_RESOURCE_PACK_POP = nextConfigurationId();
    public static final int CONFIGURATION_RESOURCE_PACK_PUSH = nextConfigurationId();
    public static final int CONFIGURATION_COOKIE_STORE = nextConfigurationId();
    public static final int CONFIGURATION_TRANSFER = nextConfigurationId();
    public static final int CONFIGURATION_UPDATE_ENABLED_FEATURES = nextConfigurationId();
    public static final int CONFIGURATION_TAGS = nextConfigurationId();
    public static final int CONFIGURATION_SELECT_KNOWN_PACKS = nextConfigurationId();
    public static final int CONFIGURATION_CUSTOM_REPORT_DETAILS = nextConfigurationId();
    public static final int CONFIGURATION_SERVER_LINKS = nextConfigurationId();

    public static final int BUNDLE = nextPlayId();
    public static final int SPAWN_ENTITY = nextPlayId();
    public static final int SPAWN_EXPERIENCE_ORB = nextPlayId();
    public static final int ENTITY_ANIMATION = nextPlayId();
    public static final int STATISTICS = nextPlayId();
    public static final int ACKNOWLEDGE_BLOCK_CHANGE = nextPlayId();
    public static final int BLOCK_BREAK_ANIMATION = nextPlayId();
    public static final int BLOCK_ENTITY_DATA = nextPlayId();
    public static final int BLOCK_ACTION = nextPlayId();
    public static final int BLOCK_CHANGE = nextPlayId();
    public static final int BOSS_BAR = nextPlayId();
    public static final int SERVER_DIFFICULTY = nextPlayId();
    public static final int CHUNK_BATCH_FINISHED = nextPlayId();
    public static final int CHUNK_BATCH_START = nextPlayId();
    public static final int CHUNK_BIOMES = nextPlayId();
    public static final int CLEAR_TITLES = nextPlayId();
    public static final int TAB_COMPLETE = nextPlayId();
    public static final int DECLARE_COMMANDS = nextPlayId();
    public static final int CLOSE_WINDOW = nextPlayId();
    public static final int WINDOW_ITEMS = nextPlayId();
    public static final int WINDOW_PROPERTY = nextPlayId();
    public static final int SET_SLOT = nextPlayId();
    public static final int COOKIE_REQUEST = nextPlayId();
    public static final int SET_COOLDOWN = nextPlayId();
    public static final int CUSTOM_CHAT_COMPLETIONS = nextPlayId();
    public static final int PLUGIN_MESSAGE = nextPlayId();
    public static final int DAMAGE_EVENT = nextPlayId();
    public static final int DEBUG_SAMPLE = nextPlayId();
    public static final int DELETE_CHAT_MESSAGE = nextPlayId();
    public static final int DISCONNECT = nextPlayId();
    public static final int DISGUISED_CHAT = nextPlayId();
    public static final int ENTITY_STATUS = nextPlayId();
    public static final int EXPLOSION = nextPlayId();
    public static final int UNLOAD_CHUNK = nextPlayId();
    public static final int CHANGE_GAME_STATE = nextPlayId();
    public static final int OPEN_HORSE_WINDOW = nextPlayId();
    public static final int HIT_ANIMATION = nextPlayId();
    public static final int INITIALIZE_WORLD_BORDER = nextPlayId();
    public static final int KEEP_ALIVE = nextPlayId();
    public static final int CHUNK_DATA = nextPlayId();
    public static final int EFFECT = nextPlayId();
    public static final int PARTICLE = nextPlayId();
    public static final int UPDATE_LIGHT = nextPlayId();
    public static final int JOIN_GAME = nextPlayId();
    public static final int MAP_DATA = nextPlayId();
    public static final int TRADE_LIST = nextPlayId();
    public static final int ENTITY_POSITION = nextPlayId();
    public static final int ENTITY_POSITION_AND_ROTATION = nextPlayId();
    public static final int ENTITY_ROTATION = nextPlayId();
    public static final int VEHICLE_MOVE = nextPlayId();
    public static final int OPEN_BOOK = nextPlayId();
    public static final int OPEN_WINDOW = nextPlayId();
    public static final int OPEN_SIGN_EDITOR = nextPlayId();
    public static final int PING = nextPlayId();
    public static final int PING_RESPONSE = nextPlayId();
    public static final int CRAFT_RECIPE_RESPONSE = nextPlayId();
    public static final int PLAYER_ABILITIES = nextPlayId();
    public static final int PLAYER_CHAT = nextPlayId();
    public static final int END_COMBAT_EVENT = nextPlayId();
    public static final int ENTER_COMBAT_EVENT = nextPlayId();
    public static final int DEATH_COMBAT_EVENT = nextPlayId();
    public static final int PLAYER_INFO_REMOVE = nextPlayId();
    public static final int PLAYER_INFO_UPDATE = nextPlayId();
    public static final int FACE_PLAYER = nextPlayId();
    public static final int PLAYER_POSITION_AND_LOOK = nextPlayId();
    public static final int UNLOCK_RECIPES = nextPlayId();
    public static final int DESTROY_ENTITIES = nextPlayId();
    public static final int REMOVE_ENTITY_EFFECT = nextPlayId();
    public static final int RESET_SCORE = nextPlayId();
    public static final int RESOURCE_PACK_POP = nextPlayId();
    public static final int RESOURCE_PACK_PUSH = nextPlayId();
    public static final int RESPAWN = nextPlayId();
    public static final int ENTITY_HEAD_LOOK = nextPlayId();
    public static final int MULTI_BLOCK_CHANGE = nextPlayId();
    public static final int SELECT_ADVANCEMENT_TAB = nextPlayId();
    public static final int SERVER_DATA = nextPlayId();
    public static final int ACTION_BAR = nextPlayId();
    public static final int WORLD_BORDER_CENTER = nextPlayId();
    public static final int WORLD_BORDER_LERP_SIZE = nextPlayId();
    public static final int WORLD_BORDER_SIZE = nextPlayId();
    public static final int WORLD_BORDER_WARNING_DELAY = nextPlayId();
    public static final int WORLD_BORDER_WARNING_REACH = nextPlayId();
    public static final int CAMERA = nextPlayId();
    public static final int HELD_ITEM_CHANGE = nextPlayId();
    public static final int UPDATE_VIEW_POSITION = nextPlayId();
    public static final int UPDATE_VIEW_DISTANCE = nextPlayId(); // Not used by the dedicated server
    public static final int SPAWN_POSITION = nextPlayId();
    public static final int DISPLAY_SCOREBOARD = nextPlayId();
    public static final int ENTITY_METADATA = nextPlayId();
    public static final int ATTACH_ENTITY = nextPlayId();
    public static final int ENTITY_VELOCITY = nextPlayId();
    public static final int ENTITY_EQUIPMENT = nextPlayId();
    public static final int SET_EXPERIENCE = nextPlayId();
    public static final int UPDATE_HEALTH = nextPlayId();
    public static final int SCOREBOARD_OBJECTIVE = nextPlayId();
    public static final int SET_PASSENGERS = nextPlayId();
    public static final int TEAMS = nextPlayId();
    public static final int UPDATE_SCORE = nextPlayId();
    public static final int SET_SIMULATION_DISTANCE = nextPlayId();
    public static final int SET_TITLE_SUBTITLE = nextPlayId();
    public static final int TIME_UPDATE = nextPlayId();
    public static final int SET_TITLE_TEXT = nextPlayId();
    public static final int SET_TITLE_TIME = nextPlayId();
    public static final int ENTITY_SOUND_EFFECT = nextPlayId();
    public static final int SOUND_EFFECT = nextPlayId();
    public static final int START_CONFIGURATION_PACKET = nextPlayId();
    public static final int STOP_SOUND = nextPlayId();
    public static final int COOKIE_STORE = nextPlayId();
    public static final int SYSTEM_CHAT = nextPlayId();
    public static final int PLAYER_LIST_HEADER_AND_FOOTER = nextPlayId();
    public static final int NBT_QUERY_RESPONSE = nextPlayId();
    public static final int COLLECT_ITEM = nextPlayId();
    public static final int ENTITY_TELEPORT = nextPlayId();
    public static final int TICK_STATE = nextPlayId();
    public static final int TICK_STEP = nextPlayId();
    public static final int TRANSFER = nextPlayId();
    public static final int ADVANCEMENTS = nextPlayId();
    public static final int ENTITY_ATTRIBUTES = nextPlayId();
    public static final int ENTITY_EFFECT = nextPlayId();
    public static final int DECLARE_RECIPES = nextPlayId();
    public static final int TAGS = nextPlayId();
    public static final int PROJECTILE_POWER = nextPlayId();
    public static final int CUSTOM_REPORT_DETAILS = nextPlayId();
    public static final int SERVER_LINKS = nextPlayId();

    private static int nextStatusId() {
        return STATUS_ID.getAndIncrement();
    }

    private static int nextLoginId() {
        return LOGIN_ID.getAndIncrement();
    }

    private static int nextConfigurationId() {
        return CONFIGURATION_ID.getAndIncrement();
    }

    private static int nextPlayId() {
        return PLAY_ID.getAndIncrement();
    }
}
