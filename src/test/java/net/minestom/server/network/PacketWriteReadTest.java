package net.minestom.server.network;

import com.google.gson.JsonObject;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.crypto.*;
import net.minestom.server.entity.*;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketRegistry;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.*;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.configuration.ClientSelectKnownPacksPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.*;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
import net.minestom.server.network.packet.server.configuration.ResetChatPacket;
import net.minestom.server.network.packet.server.configuration.SelectKnownPacksPacket;
import net.minestom.server.network.packet.server.login.*;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.network.player.ClientSettings;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.recipe.Ingredient;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.recipe.RecipeProperty;
import net.minestom.server.recipe.display.RecipeDisplay;
import net.minestom.server.recipe.display.SlotDisplay;
import net.minestom.server.utils.crypto.KeyUtils;
import net.minestom.server.world.Difficulty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ensures that packet can be written and read correctly.
 */
public class PacketWriteReadTest {
    private static final Map<Class<? extends ServerPacket>, Set<ServerPacket>> SERVER_PACKETS = new HashMap<>();
    private static final Map<Class<? extends ClientPacket>, Set<ClientPacket>> CLIENT_PACKETS = new HashMap<>();

    private static final String OG = "TheMode911";
    private static final Component COMPONENT = Component.text("Hey");
    private static final Vec VEC = new Vec(5, 5, 5);

    @SafeVarargs
    private static <T extends ServerPacket> void addServerPackets(T... packets) {
        assertNotEquals(0, packets.length);
        var packetClass = packets[0].getClass();
        var set = SERVER_PACKETS.computeIfAbsent(packetClass, c -> new HashSet<>(packets.length));
        for (var packet: packets) assertTrue(set.add(packet), "Found duplicate server packet in %s with `%s`".formatted(packet.getClass().getSimpleName(), packet));
    }

    @SafeVarargs
    private static <T extends ClientPacket> void addClientPackets(T... packets) {
        assertNotEquals(0, packets.length);
        var packetClass = packets[0].getClass();
        var set = CLIENT_PACKETS.computeIfAbsent(packetClass, c -> new HashSet<>(packets.length));
        for (var packet: packets) assertTrue(set.add(packet), "Found duplicate client packet in %s with `%s`".formatted(packet.getClass().getSimpleName(), packet));
    }

    @BeforeAll
    public static void setupServer() {
        MinecraftServer.init(); // Need some tags in here, pretty gross.

        // Handshake
        // Status
        addServerPackets(new ResponsePacket(new JsonObject().toString()));
        addServerPackets(new PingResponsePacket(5));
        // Login
        addServerPackets(
                new LoginDisconnectPacket(COMPONENT.append(Component.text(" your Disconnected!", NamedTextColor.BLUE))),
                new LoginDisconnectPacket(COMPONENT.appendNewline().appendSpace().append(Component.text("Disconnected!", NamedTextColor.RED)))
        );
        addServerPackets(
                new EncryptionRequestPacket("abvcr3ujt324joi32aaa", new byte[124], new byte[65], true), // max test
                new EncryptionRequestPacket("server", new byte[64], new byte[235], false),
                new EncryptionRequestPacket("", new byte[54], new byte[23], true) // default
        );
        addServerPackets(
                new LoginSuccessPacket(new GameProfile(UUID.randomUUID(), OG)),
                new LoginSuccessPacket(new GameProfile(UUID.randomUUID(), "APlrWith_LongNam")),
                new LoginSuccessPacket(new GameProfile(new UUID(0, 0), "8", List.of(
                        new GameProfile.Property("textures", "randomtexturethatprobablyshouldbevalidated"),
                        new GameProfile.Property("signature", "supersigned")
                )))
        );
        addServerPackets(new SetCompressionPacket(256), new SetCompressionPacket(0), new SetCompressionPacket(1024));
        addServerPackets(
                new LoginPluginRequestPacket(5, "id", new byte[16]),
                new LoginPluginRequestPacket(0, "", new byte[]{1, 2, 23, 123}),
                new LoginPluginRequestPacket(123, "id", new byte[123]),
                new LoginPluginRequestPacket(6, "somecoolChannel", new byte[]{125, 0x76, 0x32, 0x12, 0b1111}),
                new LoginPluginRequestPacket(Integer.MAX_VALUE, "x", new byte[0])
        );
        addServerPackets(
                new CookieRequestPacket("cookieKey"),
                new CookieRequestPacket(""),
                new CookieRequestPacket("minestom:cookie"),
                new CookieRequestPacket("iam/cookie")
        );
        // Configuration
        addServerPackets(new CookieRequestPacket("cookie/master")); // See above
        addServerPackets(
                new PluginMessagePacket("channel", new byte[]{1, 2, 23, 123}),
                new PluginMessagePacket("empty", new byte[124]),
                new PluginMessagePacket("", new byte[]{1, 2, 23, 123}),
                new PluginMessagePacket("", new byte[0])
        );
        addServerPackets(
                new DisconnectPacket(COMPONENT.append(Component.text(", Your gone!", NamedTextColor.RED))),
                new DisconnectPacket(COMPONENT.appendNewline().appendNewline().appendSpace().append(Component.text("Why", Style.style(NamedTextColor.RED, TextDecoration.UNDERLINED)))),
                new DisconnectPacket(Component.empty())
        );
        addServerPackets(new FinishConfigurationPacket());
        addServerPackets(
                new KeepAlivePacket(Long.MAX_VALUE),
                new KeepAlivePacket(0),
                new KeepAlivePacket(Long.MIN_VALUE),
                new KeepAlivePacket(System.currentTimeMillis())
        );
        addServerPackets(new PingPacket(0), new PingPacket(Integer.MAX_VALUE));
        addServerPackets(new ResetChatPacket());
        addServerPackets(new RegistryDataPacket("minecraft:damage_type", List.of( //TODO maybe use a proper one?
                new RegistryDataPacket.Entry("some_value", CompoundBinaryTag.builder().putString("hey", "john").build()),
                new RegistryDataPacket.Entry("some_value1", CompoundBinaryTag.builder().putInt("he5y", 1).build()),
                new RegistryDataPacket.Entry("some_value2", CompoundBinaryTag.builder().putFloat("hey2", 0.23f).build()),
                new RegistryDataPacket.Entry("some_value3", CompoundBinaryTag.builder().putString("h2ey", "john").build()),
                new RegistryDataPacket.Entry("some_value4", CompoundBinaryTag.builder().putBoolean("", true).build()),
                new RegistryDataPacket.Entry("some_value5", CompoundBinaryTag.builder().putBoolean("", false).build())
        )));
        // Play
        addServerPackets(new AcknowledgeBlockChangePacket(0));
        addServerPackets(new ActionBarPacket(COMPONENT));
        addServerPackets(new AttachEntityPacket(5, 10));
        addServerPackets(new BlockActionPacket(VEC, (byte) 5, (byte) 5, 5));
        addServerPackets(new BlockBreakAnimationPacket(5, VEC, (byte) 5));
        addServerPackets(new BlockChangePacket(VEC, 0));
        addServerPackets(new BlockEntityDataPacket(VEC, 5, CompoundBinaryTag.builder().putString("key", "value").build()));
        addServerPackets(
                new BossBarPacket(UUID.randomUUID(), new BossBarPacket.AddAction(COMPONENT, 5f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS, (byte) 2)),
                new BossBarPacket(UUID.randomUUID(), new BossBarPacket.RemoveAction()),
                new BossBarPacket(UUID.randomUUID(), new BossBarPacket.UpdateHealthAction(5f)),
                new BossBarPacket(UUID.randomUUID(), new BossBarPacket.UpdateTitleAction(COMPONENT)),
                new BossBarPacket(UUID.randomUUID(), new BossBarPacket.UpdateStyleAction(BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)),
                new BossBarPacket(UUID.randomUUID(), new BossBarPacket.UpdateFlagsAction((byte) 5))
        );
        addServerPackets(new CameraPacket(5));
        addServerPackets(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.RAIN_LEVEL_CHANGE, 2));
        addServerPackets(new SystemChatPacket(COMPONENT, false));
        addServerPackets(new ClearTitlesPacket(false));
        addServerPackets(new CloseWindowPacket((byte) 2));
        addServerPackets(new CollectItemPacket(5, 5, 5));
        var recipeDisplay = new RecipeDisplay.CraftingShapeless(
                List.of(new SlotDisplay.Item(Material.STONE)),
                new SlotDisplay.Item(Material.STONE_BRICKS),
                new SlotDisplay.Item(Material.CRAFTING_TABLE)
        );
        addServerPackets(new PlaceGhostRecipePacket(0, recipeDisplay));
        addServerPackets(new DeathCombatEventPacket(5, COMPONENT));
        addServerPackets(new DeclareRecipesPacket(Map.of(
                RecipeProperty.SMITHING_BASE, List.of(Material.STONE),
                RecipeProperty.SMITHING_TEMPLATE, List.of(Material.STONE),
                RecipeProperty.SMITHING_ADDITION, List.of(Material.STONE),
                RecipeProperty.FURNACE_INPUT, List.of(Material.STONE),
                RecipeProperty.BLAST_FURNACE_INPUT, List.of(Material.IRON_HOE, Material.DANDELION),
                RecipeProperty.SMOKER_INPUT, List.of(Material.STONE),
                RecipeProperty.CAMPFIRE_INPUT, List.of(Material.STONE)),
                List.of(new DeclareRecipesPacket.StonecutterRecipe(new Ingredient(Material.DIAMOND),
                        new SlotDisplay.ItemStack(ItemStack.of(Material.GOLD_BLOCK))))
        ));
        addServerPackets(new RecipeBookAddPacket(List.of(new RecipeBookAddPacket.Entry(1, recipeDisplay, null,
                RecipeBookCategory.CRAFTING_MISC, List.of(new Ingredient(Material.STONE)), true, true)), false));
        addServerPackets(new RecipeBookRemovePacket(List.of(1)));

        addServerPackets(new DestroyEntitiesPacket(List.of(5, 5, 5)));
        addServerPackets(new DisconnectPacket(COMPONENT));
        addServerPackets(new DisplayScoreboardPacket((byte) 5, "scoreboard"));
        addServerPackets(new WorldEventPacket(5, VEC, 5, false));
        addServerPackets(new EndCombatEventPacket(5));
        addServerPackets(new EnterCombatEventPacket());
        addServerPackets(new EntityAnimationPacket(5, EntityAnimationPacket.Animation.TAKE_DAMAGE));
        addServerPackets(new EntityEquipmentPacket(6, Map.of(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.DIAMOND_SWORD))));
        addServerPackets(new EntityHeadLookPacket(5, 90f));
        addServerPackets(new EntityMetaDataPacket(5, Map.of()));
        addServerPackets(new EntityMetaDataPacket(5, Map.of(1, Metadata.VarInt(5))));
        addServerPackets(new EntityPositionAndRotationPacket(5, (short) 0, (short) 0, (short) 0, 45f, 45f, false));
        addServerPackets(new EntityPositionPacket(5, (short) 0, (short) 0, (short) 0, true));
        addServerPackets(new EntityAttributesPacket(5, List.of()));
        addServerPackets(new EntityRotationPacket(5, 45f, 45f, false));

        final PlayerSkin skin = new PlayerSkin("hh", "hh");
        List<PlayerInfoUpdatePacket.Property> prop = List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature()));

        addServerPackets(
                new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), OG, prop, false, 0, null, null, null, 0)),
                new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), null, List.of(), false, 0, null, Component.text("Not").append(Component.text(OG)), null, 0)),
                new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), null, List.of(), false, 0, GameMode.CREATIVE, null, null, 0)),
                new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), null, List.of(), false, 20, null, null, null, 0)),
                new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), null, List.of(), true, 0, null, null, null, 0)),
                new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER,
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), null, List.of(), false, 0, null, null, null, 42)),
        new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_HAT,
                new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), "", List.of(), false, 0, GameMode.SURVIVAL, null, null, 0, false))
        );

        addServerPackets(new PlayerInfoRemovePacket(UUID.randomUUID()));
    }

    @BeforeAll
    public static void setupClient() {
        MinecraftServer.init(); // Need to validate packets with auth mode.

        // Handshake
        addClientPackets(
                new ClientHandshakePacket(755, "localhost", 25565, ClientHandshakePacket.Intent.LOGIN),
                new ClientHandshakePacket(Integer.MAX_VALUE, "localhost", 25565, ClientHandshakePacket.Intent.LOGIN),
                new ClientHandshakePacket(Integer.MIN_VALUE, "localhost", 25565, ClientHandshakePacket.Intent.LOGIN),
                new ClientHandshakePacket(6321, "localhost", 25565, ClientHandshakePacket.Intent.STATUS),
                new ClientHandshakePacket(12341, "transfer.example.com", 25565, ClientHandshakePacket.Intent.TRANSFER)
        );

        // Status
        addClientPackets(
                new StatusRequestPacket()
        );
        addClientPackets(
                new ClientPingRequestPacket(Long.MIN_VALUE),
                new ClientPingRequestPacket(Long.MAX_VALUE),
                new ClientPingRequestPacket(0)
        );

        addClientPackets(
                new ClientPongPacket(Integer.MAX_VALUE), new ClientPongPacket(Integer.MIN_VALUE), new ClientPongPacket(6500125), new ClientPongPacket(0)
        );

        // Login
        addClientPackets(
                new ClientLoginStartPacket("APlrWith_LongNam", UUID.randomUUID()),
                new ClientLoginStartPacket("", UUID.randomUUID()),
                new ClientLoginStartPacket(OG, UUID.randomUUID()),
                new ClientLoginStartPacket(OG, new UUID(0, 0))
        );
        addClientPackets(
                new ClientEncryptionResponsePacket(new byte[123], new byte[123]),
                new ClientEncryptionResponsePacket(new byte[0], new byte[0]),
                new ClientEncryptionResponsePacket(new byte[]{1, 21, 3, 0x04}, new byte[]{1, 2, 74, 4})
        );
        addClientPackets(
                new ClientLoginPluginResponsePacket(1, new byte[]{1, 2, 3, 4}),
                new ClientLoginPluginResponsePacket(0, new byte[0]),
                new ClientLoginPluginResponsePacket(Integer.MAX_VALUE, new byte[]{1, 2, 3, 4, 5, 6}),
                new ClientLoginPluginResponsePacket(Integer.MIN_VALUE, new byte[123])
        );
        addClientPackets(new ClientLoginAcknowledgedPacket());
        addClientPackets(
                new ClientCookieResponsePacket("minestom:cookie", new byte[123]),
                new ClientCookieResponsePacket("cookie", new byte[0]),
                new ClientCookieResponsePacket("cookie/packet", new byte[]{1, 22, 36, 42, -51, 6}),
                new ClientCookieResponsePacket("cookie/max", new byte[5120]) // max length
        );
        // Configuration
        addClientPackets(
                new ClientSettingsPacket(ClientSettings.DEFAULT),
                new ClientSettingsPacket(new ClientSettings(
                        Locale.UK, (byte) 2, ChatMessageType.FULL,false,
                        (byte) 0x01, ClientSettings.MainHand.LEFT,
                        false, false,
                        ClientSettings.ParticleSetting.MINIMAL
                )),
                new ClientSettingsPacket(new ClientSettings(
                        Locale.CANADA_FRENCH, (byte) 32,
                        ChatMessageType.SYSTEM, true,
                        (byte) 0x7F, ClientSettings.MainHand.RIGHT,
                        true, false,
                        ClientSettings.ParticleSetting.DECREASED
                )),
                new ClientSettingsPacket(new ClientSettings(
                        Locale.GERMANY, (byte) 12,
                        ChatMessageType.FULL, true,
                        (byte) 0x3F, ClientSettings.MainHand.LEFT,
                        true, false,
                        ClientSettings.ParticleSetting.ALL
                )),
                new ClientSettingsPacket(new ClientSettings(
                        Locale.JAPAN, (byte) 4,
                        ChatMessageType.NONE, true,
                        (byte) 0x7F, ClientSettings.MainHand.RIGHT,
                        true, true,
                        ClientSettings.ParticleSetting.ALL
                )),
                new ClientSettingsPacket(new ClientSettings(
                        Locale.FRANCE, (byte) 8,
                        ChatMessageType.SYSTEM, false,
                        (byte) 0x2A, ClientSettings.MainHand.LEFT,
                        false, true,
                        ClientSettings.ParticleSetting.MINIMAL
                ))
        );
        addClientPackets(new ClientCookieResponsePacket("cookie/master", new byte[]{127, -128})); // See above
        addClientPackets(
                new ClientPluginMessagePacket("channel", new byte[]{-128, -128, -128, 0, 127}),
                new ClientPluginMessagePacket("empty", new byte[0]),
                new ClientPluginMessagePacket("", new byte[]{1, 2, 3, 4}),
                new ClientPluginMessagePacket("", new byte[123])
        );
        addClientPackets(new ClientConfigurationAckPacket());
        addClientPackets(new ClientKeepAlivePacket(System.nanoTime())); // Incorrect but should still work.
        addClientPackets(new ClientPingRequestPacket(-1)); // See above.
        for (ResourcePackStatus status : ResourcePackStatus.values()) { // Full enum test
            addClientPackets(new ClientResourcePackStatusPacket(UUID.randomUUID(), status));
        }
        addClientPackets(new ClientSelectKnownPacksPacket(List.of(
                new SelectKnownPacksPacket.Entry("namespaced:entry", "custom_id", "1.0.0"),
                new SelectKnownPacksPacket.Entry("defaultnamespace", "other_id", "12598125")
        )));
        addClientPackets(
                new ClientCustomClickActionPacket(Key.key("wowzers"), CompoundBinaryTag.builder().putInt("key", 0).build()),
                new ClientCustomClickActionPacket(Key.key("asgdf"), CompoundBinaryTag.builder().putString("key", "value").build())
        );

        // Play
        addClientPackets(new ClientTeleportConfirmPacket(325626), new ClientTeleportConfirmPacket(Integer.MAX_VALUE), new ClientTeleportConfirmPacket(Integer.MIN_VALUE));
        addClientPackets(new ClientQueryBlockNbtPacket(1325, BlockVec.ZERO), new ClientQueryBlockNbtPacket(-15, BlockVec.ONE));
        addClientPackets(new ClientSelectBundleItemPacket(32, 65), new ClientSelectBundleItemPacket(Integer.MAX_VALUE, Integer.MAX_VALUE));
        addClientPackets(new ClientChangeDifficultyPacket(Difficulty.EASY, false),  new ClientChangeDifficultyPacket(Difficulty.HARD, true), new ClientChangeDifficultyPacket(Difficulty.PEACEFUL, true));
        addClientPackets(new ClientChangeGameModePacket(GameMode.ADVENTURE),  new ClientChangeGameModePacket(GameMode.SURVIVAL), new ClientChangeGameModePacket(GameMode.CREATIVE), new ClientChangeGameModePacket(GameMode.SPECTATOR));
        addClientPackets(new ClientChatAckPacket(12549581), new ClientChatAckPacket(Integer.MIN_VALUE), new ClientChatAckPacket(Integer.MAX_VALUE));
        addClientPackets(new ClientCommandChatPacket("l".repeat(256)), new ClientCommandChatPacket("helloworld"));
        //TODO (signed) support signed chat/commands with proper data.
        addClientPackets(new ClientSignedCommandChatPacket("helloworld", Long.MAX_VALUE, 0L, new ArgumentSignatures(List.of(new ArgumentSignatures.Entry("hey", new MessageSignature(new byte[256])))), new LastSeenMessages.Update(100, new BitSet()), (byte) 0));
        addClientPackets(new ClientChatMessagePacket("My name is bob", Long.MAX_VALUE, 0L, new byte[10], 100, new BitSet(), (byte) 100));
        //TODO (signed) use a key for tests
        addClientPackets(new ClientChatSessionUpdatePacket(new ChatSession(UUID.randomUUID(), new PlayerPublicKey(Instant.MAX, new PublicKey() {
            @Override
            public String getAlgorithm() {
                return "";
            }

            @Override
            public String getFormat() {
                return "";
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }
        }, new byte[4096]))));
        addClientPackets(new ClientChunkBatchReceivedPacket(0.5f));
        addClientPackets(new ClientStatusPacket(ClientStatusPacket.Action.PERFORM_RESPAWN), new  ClientStatusPacket(ClientStatusPacket.Action.REQUEST_STATS));
        addClientPackets(new ClientTickEndPacket());
        addClientPackets(new ClientTabCompletePacket(15, "/hellloworld"), new ClientTabCompletePacket(Integer.MIN_VALUE, "/hello arg1 arg2 arg3"), new ClientTabCompletePacket(-1000, "//undo"));
        addClientPackets(new ClientFinishConfigurationPacket());
        addClientPackets(new ClientClickWindowButtonPacket(15, 14), new ClientClickWindowButtonPacket(Integer.MIN_VALUE, Integer.MAX_VALUE));
        addClientPackets(new ClientClickWindowPacket(125, 20, (short) -999, (byte) 1, ClientClickWindowPacket.ClickType.SWAP, Map.of(), ItemStack.Hash.AIR), new ClientClickWindowPacket(Integer.MAX_VALUE, Integer.MIN_VALUE, (short) 51, (byte) 1, ClientClickWindowPacket.ClickType.SWAP, Map.of((short) 5, ItemStack.Hash.AIR), ItemStack.Hash.AIR));
        addClientPackets(new ClientCloseWindowPacket(15), new ClientCloseWindowPacket(Integer.MIN_VALUE));
        addClientPackets(new ClientWindowSlotStatePacket(25, 25, true), new ClientWindowSlotStatePacket(Integer.MAX_VALUE, Integer.MAX_VALUE, true), new  ClientWindowSlotStatePacket(Integer.MIN_VALUE, Integer.MAX_VALUE, false));
        //Cookie
        //Plugin message
        addClientPackets(new ClientDebugSampleSubscriptionPacket(DebugSamplePacket.Type.TICK_TIME));
        addClientPackets(new ClientEditBookPacket(14, List.of("page1", "page2"), "Wrath of nothing"), new ClientEditBookPacket(15, List.of(), null), new ClientEditBookPacket(12, List.of("hi".repeat(100).split("h")), "What is this book?"));
        addClientPackets(new ClientQueryEntityNbtPacket(1325, 25), new ClientQueryEntityNbtPacket(-15, Integer.MAX_VALUE));
        addClientPackets(
                new ClientInteractEntityPacket(10, new ClientInteractEntityPacket.Attack(), true),
                new ClientInteractEntityPacket(32, new ClientInteractEntityPacket.Interact(PlayerHand.MAIN), false),
                new ClientInteractEntityPacket(15, new ClientInteractEntityPacket.InteractAt(1f, 2f, 1f, PlayerHand.MAIN), true),
                new ClientInteractEntityPacket(Integer.MAX_VALUE, new ClientInteractEntityPacket.Interact(PlayerHand.OFF), false),
                new ClientInteractEntityPacket(2365, new ClientInteractEntityPacket.Attack(), false)
        );
        addClientPackets(new ClientGenerateStructurePacket(Vec.ZERO, Integer.MAX_VALUE, true));
        addClientPackets(new ClientLockDifficultyPacket(true), new ClientLockDifficultyPacket(false));
        addClientPackets(new ClientPlayerPositionPacket(Vec.ONE, (byte) ClientPlayerPositionPacket.FLAG_HORIZONTAL_COLLISION), new ClientPlayerPositionPacket(Vec.ZERO, (byte) ClientPlayerPositionPacket.FLAG_ON_GROUND));
        addClientPackets(new ClientPlayerPositionAndRotationPacket(Pos.ZERO, true, true), new ClientPlayerPositionAndRotationPacket(new Pos(10, 10, 10, 0f, 0f), false, true));
        addClientPackets(new ClientPlayerPositionStatusPacket(true, false),  new ClientPlayerPositionStatusPacket(false, false), new  ClientPlayerPositionStatusPacket(false, true), new  ClientPlayerPositionStatusPacket(true, true));
        addClientPackets(new ClientVehicleMovePacket(new Pos(5, 5, 5, 45f, 45f), true));
        addClientPackets(new ClientVehicleMovePacket(new Pos(6, 5, 6, 82f, 12.5f), false));
        addClientPackets(new ClientSteerBoatPacket(true, false), new ClientSteerBoatPacket(false, false), new  ClientSteerBoatPacket(true, true), new ClientSteerBoatPacket(false, true));
        addClientPackets(new ClientPickItemFromBlockPacket(BlockVec.ONE, true), new ClientPickItemFromBlockPacket(BlockVec.ZERO, false));
        addClientPackets(new ClientPickItemFromEntityPacket(124, true), new ClientPickItemFromEntityPacket(124, false), new   ClientPickItemFromEntityPacket(Integer.MAX_VALUE, true), new ClientPickItemFromEntityPacket(Integer.MIN_VALUE, false));
        addClientPackets(new ClientPlaceRecipePacket((byte) 10, 10, true), new ClientPlaceRecipePacket((byte) 51, 14, false));
        addClientPackets(new ClientPlayerAbilitiesPacket((byte) 0x02));
        addClientPackets(new ClientPlayerDiggingPacket(ClientPlayerDiggingPacket.Status.STARTED_DIGGING, BlockVec.ZERO, BlockFace.BOTTOM, Integer.MAX_VALUE), new ClientPlayerDiggingPacket(ClientPlayerDiggingPacket.Status.DROP_ITEM_STACK, BlockVec.ONE, BlockFace.TOP, Integer.MIN_VALUE));
        addClientPackets(new ClientEntityActionPacket(10, ClientEntityActionPacket.Action.LEAVE_BED, 0), new  ClientEntityActionPacket(15, ClientEntityActionPacket.Action.START_SPRINTING, 0), new  ClientEntityActionPacket(321, ClientEntityActionPacket.Action.START_FLYING_ELYTRA, 0));
        addClientPackets(new ClientInputPacket(true, false, true, false, false, false, true), new ClientInputPacket(false, true, true, false, false, false, true));
        addClientPackets(new ClientPlayerLoadedPacket());
        //addClientPackets(new ClientSetRecipeBookStatePacket());
    }

    private static <T> void testPacket(NetworkBuffer.Type<T> networkType, T packet) {
        byte[] bytes = NetworkBuffer.makeArray(networkType, packet);
        var buffer = NetworkBuffer.wrap(bytes, 0, bytes.length);
        var createdPacket = buffer.read(networkType);
        assertEquals(packet, createdPacket);
    }

    static <T> Stream<PacketRegistry.PacketInfo<? extends T>> packets(PacketParser<T> parser) {
        return Stream.of(
                parser.handshake(),
                parser.status(),
                parser.login(),
                parser.configuration()//,
                //parser.play()
        ).flatMap(registry -> registry.packets().stream());
    }

    static Stream<Arguments> serverPacketArguments() {
        return packets(PacketVanilla.SERVER_PACKET_PARSER)
                .flatMap(info -> {
                    var tests = SERVER_PACKETS.get(info.packetClass());
                    var name = info.packetClass().getSimpleName();
                    assertNotNull(tests, "No server packet tests for " + name);
                    assertNotEquals(0, tests.size(), "Empty server packet tests for " + name);

                    return tests.stream().map(packet ->
                            Arguments.of(info.serializer(), packet, name)
                    );
                });
    }

    static Stream<Arguments> clientPacketArguments() {
        return packets(PacketVanilla.CLIENT_PACKET_PARSER)
                .flatMap(info -> {
                    var tests = CLIENT_PACKETS.get(info.packetClass());
                    var name = info.packetClass().getSimpleName();
                    assertNotNull(tests, "No client packet tests for " + name);
                    assertNotEquals(0, tests.size(), "Empty client packet tests for " + name);

                    return tests.stream().map(packet ->
                            Arguments.of(info.serializer(), packet, name)
                    );
                });
    }

    @ParameterizedTest(name = "Server Packet Test: {2}")
    @MethodSource("serverPacketArguments")
    void serverTest(NetworkBuffer.Type<ServerPacket> serializer, ServerPacket packet, String ignoredFormatName) {
        testPacket(serializer, packet);
    }

    @ParameterizedTest(name = "Client Packet Test: {2}")
    @MethodSource("clientPacketArguments")
    void clientTest(NetworkBuffer.Type<ClientPacket> serializer, ClientPacket packet, String ignoredFormatName) {
        testPacket(serializer, packet);
    }
}
