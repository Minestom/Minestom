package net.minestom.server.network;

import com.google.gson.JsonObject;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.crypto.*;
import net.minestom.server.dialog.*;
import net.minestom.server.entity.*;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockEntityType;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.debug.DebugSubscription;
import net.minestom.server.network.debug.info.DebugHiveInfo;
import net.minestom.server.network.debug.info.DebugPathInfo;
import net.minestom.server.network.debug.info.DebugPoiInfo;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketRegistry;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.*;
import net.minestom.server.network.packet.client.configuration.ClientAcceptCodeOfConductPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.configuration.ClientSelectKnownPacksPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.client.status.ClientStatusRequestPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.*;
import net.minestom.server.network.packet.server.configuration.*;
import net.minestom.server.network.packet.server.login.*;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.network.player.ClientSettings;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.recipe.Ingredient;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.recipe.RecipeProperty;
import net.minestom.server.recipe.display.RecipeDisplay;
import net.minestom.server.recipe.display.SlotDisplay;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.statistic.StatisticCategory;
import net.minestom.server.utils.Rotation;
import net.minestom.server.world.Difficulty;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ensures that packet can be written and read correctly.
 */
@EnvTest // Some packets require registries.
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
        for (var packet : packets)
            assertTrue(set.add(packet), "Found duplicate server packet in %s with `%s`".formatted(packet.getClass().getSimpleName(), packet));
    }

    @SafeVarargs
    private static <T extends ClientPacket> void addClientPackets(T... packets) {
        assertNotEquals(0, packets.length);
        var packetClass = packets[0].getClass();
        var set = CLIENT_PACKETS.computeIfAbsent(packetClass, c -> new HashSet<>(packets.length));
        for (var packet : packets)
            assertTrue(set.add(packet), "Found duplicate client packet in %s with `%s`".formatted(packet.getClass().getSimpleName(), packet));
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
        addServerPackets(new ResourcePackPushPacket(new UUID(Long.MAX_VALUE, 0), "test", "test", false, Component.text("hello").append(COMPONENT)));
        addServerPackets(new ResourcePackPopPacket(new UUID(Long.MAX_VALUE, 0)), new ResourcePackPopPacket(new UUID(0, Long.MAX_VALUE)));
        addServerPackets(new CookieStorePacket("somepacket", new byte[]{1, 2, 23, 123}), new CookieStorePacket("somepacket", new byte[5120]));
        addServerPackets(new TransferPacket("test", 20000), new TransferPacket("0", 25565));
        addServerPackets(new UpdateEnabledFeaturesPacket(List.of("unvalidated", "very")));
        addServerPackets(new TagsPacket(List.of(new TagsPacket.Registry("test", List.of(new TagsPacket.Tag("#cool", new int[]{1, 2, 23, 123}))))), new TagsPacket(List.of()));
        addServerPackets(new SelectKnownPacksPacket(List.of(new SelectKnownPacksPacket.Entry("test", "id", "randomversion"))));
        addServerPackets(new CustomReportDetailsPacket(Map.of("key", "value", "key1", "value1")));
        addServerPackets(new ServerLinksPacket(new ServerLinksPacket.Entry(ServerLinksPacket.KnownLinkType.BUG_REPORT, "https://minestom.net"), new ServerLinksPacket.Entry(ServerLinksPacket.KnownLinkType.ANNOUNCEMENTS, "https://minestom.net")));
        addServerPackets(new ClearDialogPacket());
        addServerPackets(new ShowDialogPacket(
                new Dialog.MultiAction(
                        new DialogMetadata(COMPONENT, COMPONENT, true, false, DialogAfterAction.WAIT_FOR_RESPONSE, List.of(), List.of(new DialogInput.Text("heyt", 12, COMPONENT, true, "", 10, null))),
                        List.of(),
                        null,
                        10
                )));
        addServerPackets(new ShowDialogPacket(
                new Dialog.Confirmation(
                        new DialogMetadata(COMPONENT, COMPONENT.append(Component.text(OG)), true, false, DialogAfterAction.WAIT_FOR_RESPONSE, List.of(), List.of(new DialogInput.Text("heyt", 12, COMPONENT, true, "", 10, null))),
                        new DialogActionButton(COMPONENT.appendNewline(), COMPONENT, DialogActionButton.DEFAULT_WIDTH, new DialogAction.OpenUrl("https://minestom.net")),
                        new DialogActionButton(COMPONENT.appendNewline(), COMPONENT, 10, new DialogAction.CopyToClipboard("https://minestom.net"))
                )));
        addServerPackets(new CodeOfConductPacket("You need to be a nice person, i think?"));
        // Play
        addServerPackets(new AcknowledgeBlockChangePacket(0));
        addServerPackets(new ActionBarPacket(COMPONENT));
        addServerPackets(new AttachEntityPacket(5, 10));
        addServerPackets(new BlockActionPacket(VEC, (byte) 5, (byte) 5, 5));
        addServerPackets(new BlockBreakAnimationPacket(5, VEC, (byte) 5));
        addServerPackets(new BlockChangePacket(VEC, 0));
        addServerPackets(new BlockEntityDataPacket(VEC, BlockEntityType.SIGN, CompoundBinaryTag.builder().putString("key", "value").build()));
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
        addServerPackets( // TODO, these test are highly dependent on the default values, which arent great.
                new PlayerInfoUpdatePacket(
                        EnumSet.of(
                                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                                PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE
                        ),
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), OG, List.of(new GameProfile.Property("textures", skin.textures(), skin.signature())), false, 0, GameMode.CREATIVE, null, null, 0, true)
                ),
                new PlayerInfoUpdatePacket(
                        EnumSet.of(
                                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                                PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                                PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                                PlayerInfoUpdatePacket.Action.UPDATE_HAT
                        ),
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), "", List.of(), false, 0, GameMode.CREATIVE, Component.text("Not").append(Component.text(OG)), null, 0, false)
                ),
                new PlayerInfoUpdatePacket(
                        EnumSet.of(
                                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                                PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE
                        ),
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), "", List.of(), false, 0, GameMode.SPECTATOR, null, null, 0, true)
                ),
                new PlayerInfoUpdatePacket(
                        EnumSet.of(
                                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                                PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                                PlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
                                PlayerInfoUpdatePacket.Action.UPDATE_HAT
                        ),
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), "", List.of(), false, 20, GameMode.CREATIVE, null, null, 0, false)
                ),
                new PlayerInfoUpdatePacket(
                        EnumSet.of(
                                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                                PlayerInfoUpdatePacket.Action.UPDATE_LISTED
                        ),
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), "", List.of(), true, 0, GameMode.SURVIVAL, null, null, 0, true)
                ),
                new PlayerInfoUpdatePacket(
                        EnumSet.of(
                                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                                PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                                PlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER,
                                PlayerInfoUpdatePacket.Action.UPDATE_HAT
                        ),
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), "", List.of(), false, 0, GameMode.CREATIVE, null, null, 42, false)
                ),
                new PlayerInfoUpdatePacket(
                        EnumSet.of(
                                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                                PlayerInfoUpdatePacket.Action.UPDATE_HAT
                        ),
                        new PlayerInfoUpdatePacket.Entry(UUID.randomUUID(), "", List.of(), false, 0, GameMode.SURVIVAL, null, null, 0, false)
                )
        );

        addServerPackets(new PlayerInfoRemovePacket(UUID.randomUUID()));
        addServerPackets(new EntitySoundEffectPacket(SoundEvent.ENTITY_PLAYER_HURT, Sound.Source.PLAYER, 5, 1.0f, 1.0f, 0L));
        addServerPackets(new EntityStatusPacket(5, (byte) 2));
        addServerPackets(new EntityTeleportPacket(5, new Pos(0, 64, 0, 0, 0), Vec.ZERO, RelativeFlags.NONE, false));
        addServerPackets(new EntityVelocityPacket(5, Vec.ONE));
        addServerPackets(new ExplosionPacket(VEC, 4.0f, 3, null, Particle.FLAME, SoundEvent.ENTITY_GENERIC_EXPLODE, List.of()));
        addServerPackets(new FacePlayerPacket(FacePlayerPacket.FacePosition.EYES, VEC, null));
        addServerPackets(new HeldItemChangePacket((byte) 0));
        addServerPackets(new HitAnimationPacket(5, 90f));
        addServerPackets(new InitializeWorldBorderPacket(0.0, 0.0, 10.0, 5.0, 0L, 29999984, 5, 15));
        addServerPackets(new JoinGamePacket(5, false, List.of("minecraft:overworld"), 0, 10, 10, false, true, false, 0, "minecraft:overworld", 0L, GameMode.CREATIVE, GameMode.SURVIVAL, false, false, null, 0, 0, false));
        addServerPackets(new MapDataPacket(5, (byte) 1, true, true, List.of(), null));
        addServerPackets(new MultiBlockChangePacket(0, 0, 0, new long[0]));
        addServerPackets(new NbtQueryResponsePacket(5, CompoundBinaryTag.builder().putString("key", "value").build()));
        addServerPackets(new OpenBookPacket(PlayerHand.MAIN));
        addServerPackets(new OpenHorseWindowPacket((byte) 5, 5, 5));
        addServerPackets(new OpenSignEditorPacket(VEC, true));
        addServerPackets(new OpenWindowPacket(5, 5, COMPONENT));
        addServerPackets(new ParticlePacket(Particle.FLAME, VEC, Vec.ZERO, 0.1f, 10));
        addServerPackets(new PlayerAbilitiesPacket((byte) 0x0F, 0.05f, 0.1f));
        addServerPackets(new PlayerListHeaderAndFooterPacket(COMPONENT, COMPONENT));
        addServerPackets(new PlayerPositionAndLookPacket(5, VEC, Vec.ZERO, 0f, 0f, 0));
        addServerPackets(new PlayerRotationPacket(45f, false, 90f, false));
        addServerPackets(new ProjectilePowerPacket(5, 1.0));
        addServerPackets(new RespawnPacket(0,"overworld", 0L, GameMode.CREATIVE, GameMode.SURVIVAL, false, false, null, 5, 63, RespawnPacket.COPY_METADATA));
        addServerPackets(
                new ScoreboardObjectivePacket("objective", new ScoreboardObjectivePacket.Create(COMPONENT, ScoreboardObjectivePacket.Type.HEARTS, Sidebar.NumberFormat.blank())),
                new ScoreboardObjectivePacket("objective", new ScoreboardObjectivePacket.Create(COMPONENT, ScoreboardObjectivePacket.Type.HEARTS, null)),
                new ScoreboardObjectivePacket("objective", new ScoreboardObjectivePacket.Destroy()),
                new ScoreboardObjectivePacket("objective", new ScoreboardObjectivePacket.Update(COMPONENT, ScoreboardObjectivePacket.Type.HEARTS, Sidebar.NumberFormat.styled(Component.empty()))),
                new ScoreboardObjectivePacket("objective", new ScoreboardObjectivePacket.Update(COMPONENT, ScoreboardObjectivePacket.Type.HEARTS, null)))
        ;
        addServerPackets(new SelectAdvancementTabPacket("minecraft:story/root"));
        addServerPackets(new ServerDataPacket(COMPONENT, null));
        addServerPackets(new ServerDifficultyPacket(Difficulty.NORMAL, true));
        addServerPackets(new SetCooldownPacket("minecraft:ender_pearl", 5));
        addServerPackets(new SetCursorItemPacket(ItemStack.of(Material.DIAMOND)));
        addServerPackets(new SetExperiencePacket(0.5f, 10, 5));
        addServerPackets(new SetPassengersPacket(5, List.of(6, 7)));
        addServerPackets(new SetPlayerInventorySlotPacket(36, ItemStack.of(Material.DIAMOND_SWORD)));
        addServerPackets(new SetSlotPacket((byte) 0, 0, (short) 36, ItemStack.of(Material.DIAMOND)));
        addServerPackets(new SetTickStatePacket(20.0f, false));
        addServerPackets(new SetTitleSubTitlePacket(COMPONENT));
        addServerPackets(new SetTitleTextPacket(COMPONENT));
        addServerPackets(new SetTitleTimePacket(10, 70, 20));
        addServerPackets(new SoundEffectPacket(SoundEvent.ENTITY_PLAYER_HURT, net.kyori.adventure.sound.Sound.Source.PLAYER, VEC.blockX(), VEC.blockY(), VEC.blockZ(), 1.0f, 1.0f, 0L));
        addServerPackets(new SpawnEntityPacket(5, UUID.randomUUID(), EntityType.ZOMBIE, new Pos(0, 64, 0, 0, 0), 9.84375f, 0, Vec.ONE));
        addServerPackets(new SpawnPositionPacket(new WorldPos("overworld", VEC), 0f, 1f));
        addServerPackets(new StartConfigurationPacket());
        addServerPackets(new StatisticsPacket(List.of(new StatisticsPacket.Statistic(StatisticCategory.BROKEN, 5, 100))));
        addServerPackets(
                new StopSoundPacket(new StopSoundPacket.All()),
                new StopSoundPacket(new StopSoundPacket.Source(Sound.Source.BLOCK)),
                new StopSoundPacket(new StopSoundPacket.Sound("minecraft:block.amethyst_block.break")),
                new StopSoundPacket(new StopSoundPacket.SourceAndSound(Sound.Source.BLOCK, "block.amethyst_block.break"))
        );
        addServerPackets(new TabCompletePacket(5, 0, 0, List.of()));
        addServerPackets(new TeamsPacket("team", new TeamsPacket.CreateTeamAction(COMPONENT, (byte) 0, TeamsPacket.NameTagVisibility.ALWAYS, TeamsPacket.CollisionRule.ALWAYS, NamedTextColor.RED, COMPONENT, COMPONENT, List.of("player1"))));
        addServerPackets(new TickStepPacket(20));
        addServerPackets(new TimeUpdatePacket(1000L, 6000L, false));
        addServerPackets(new TradeListPacket(5, List.of(), 5, 5, true, true));
        addServerPackets(new UnloadChunkPacket(0, 0));
        addServerPackets(new UpdateHealthPacket(20.0f, 20, 5.0f));
        addServerPackets(new UpdateScorePacket("player", "objective", 100, COMPONENT, null));
        addServerPackets(new UpdateSimulationDistancePacket(8));
        addServerPackets(new UpdateViewDistancePacket(10));
        addServerPackets(new UpdateViewPositionPacket(0, 0));
        addServerPackets(new VehicleMovePacket(new Pos(0, 64, 0, 0, 0)));
        addServerPackets(new WindowItemsPacket((byte) 0, 0, List.of(ItemStack.of(Material.DIAMOND)), ItemStack.of(Material.STONE)));
        addServerPackets(new WindowPropertyPacket((byte) 0, (short) 0, (short) 5));
        addServerPackets(new WorldBorderCenterPacket(0.0, 0.0));
        addServerPackets(new WorldBorderLerpSizePacket(10.0, 20.0, 5000L));
        addServerPackets(new WorldBorderSizePacket(10.0));
        addServerPackets(new WorldBorderWarningDelayPacket(5));
        addServerPackets(new WorldBorderWarningReachPacket(5));
        addServerPackets(new AdvancementsPacket(false, List.of(), List.of(), List.of(), true));
        // TODO, these chunk* skips important paths
        addServerPackets(new ChunkBatchStartPacket());
        addServerPackets(new ChunkBatchFinishedPacket(100));
        addServerPackets(new ChunkDataPacket(0, 0, new ChunkData(Map.of(), new byte[0], Map.of()), new LightData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of())));
        addServerPackets(new ChunkBiomesPacket(List.of()));
        addServerPackets(new CustomChatCompletionPacket(CustomChatCompletionPacket.Action.ADD, List.of("entry1", "entry2")));
        addServerPackets(new DamageEventPacket(5, 1, 2, 3, VEC));
        addServerPackets(new DeclareCommandsPacket(List.of(), 0));
        addServerPackets(new BundlePacket());
        addServerPackets(new DebugBlockValuePacket(Vec.ONE, new DebugSubscription.Update<>(DebugSubscription.BEE_HIVES, new DebugHiveInfo(Block.BEEHIVE, 1, 0, true))));
        addServerPackets(new DebugChunkValuePacket(1, 1, new DebugSubscription.Update<>(DebugSubscription.POIS, new DebugPoiInfo(VEC, DebugPoiInfo.Type.BUTCHER, 1))));
        addServerPackets(new DebugEntityValuePacket(0, new DebugSubscription.Update<>(DebugSubscription.ENTITY_PATHS, new DebugPathInfo(new DebugPathInfo.Path(true, 0, VEC, List.of(), new DebugPathInfo.Data(Set.of(), List.of(), List.of())), 1))));
        addServerPackets(new DebugEventPacket(new DebugSubscription.Event<>(DebugSubscription.NEIGHBOR_UPDATES, Vec.ZERO)));
        addServerPackets(new DebugSamplePacket(new long[0], DebugSamplePacket.Type.TICK_TIME)); // Legacy debug wrapper, maybe it will change.
        addServerPackets(new DeleteChatPacket(new MessageSignature(new byte[256])));
        addServerPackets(new DisguisedChatPacket(Component.text("Hey"), 0, Component.text("Message"), null));
        addServerPackets(new EntityPositionSyncPacket(1, VEC, VEC, 1f, 1f, false));
        addServerPackets(new GameTestHighlightPosPacket(VEC, VEC));
        addServerPackets(new UpdateLightPacket(0, 0, new LightData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of())));
        addServerPackets(new MoveMinecartPacket(1, List.of(new MoveMinecartPacket.LerpStep(VEC, Vec.ZERO, 1f, 1f, 1f))));
        addServerPackets(new PlayerChatMessagePacket(0, UUID.randomUUID(), 0, new MessageSignature(new byte[256]), new SignedMessageBody.Packed("hey", Instant.EPOCH, 0L, new LastSeenMessages.Packed(List.of())), null, new FilterMask(FilterMask.Type.FULLY_FILTERED, new BitSet()), 1, Component.text("hey"), null));
        addServerPackets(new RecipeBookSettingsPacket(false, false, true, false, false, false, false, false));
        addServerPackets(new RemoveEntityEffectPacket(0, PotionEffect.BAD_OMEN));
        addServerPackets(new ResetScorePacket("dummy_score", null), new ResetScorePacket("duoka", "testObjective"));
        addServerPackets(new TestInstanceBlockStatus(Component.text("Minestom is cool"), null), new TestInstanceBlockStatus(Component.text("Where is season 5 william?"), VEC));
        addServerPackets(new EntityEffectPacket(0, new Potion(PotionEffect.ABSORPTION, 1, 150)));
        addServerPackets(new TrackedWaypointPacket(TrackedWaypointPacket.Operation.UNTRACK, new TrackedWaypointPacket.Waypoint("test", TrackedWaypointPacket.Icon.DEFAULT, new TrackedWaypointPacket.Target.Empty())));
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
                new ClientStatusRequestPacket()
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
                        Locale.UK, (byte) 2, ChatMessageType.FULL, false,
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
        addClientPackets(new ClientAcceptCodeOfConductPacket());

        // Play
        addClientPackets(new ClientTeleportConfirmPacket(325626), new ClientTeleportConfirmPacket(Integer.MAX_VALUE), new ClientTeleportConfirmPacket(Integer.MIN_VALUE));
        addClientPackets(new ClientQueryBlockNbtPacket(1325, Vec.ZERO), new ClientQueryBlockNbtPacket(-15, Vec.ONE));
        addClientPackets(new ClientSelectBundleItemPacket(32, 65), new ClientSelectBundleItemPacket(Integer.MAX_VALUE, Integer.MAX_VALUE));
        addClientPackets(new ClientChangeDifficultyPacket(Difficulty.EASY, false), new ClientChangeDifficultyPacket(Difficulty.HARD, true), new ClientChangeDifficultyPacket(Difficulty.PEACEFUL, true));
        addClientPackets(new ClientChangeGameModePacket(GameMode.ADVENTURE), new ClientChangeGameModePacket(GameMode.SURVIVAL), new ClientChangeGameModePacket(GameMode.CREATIVE), new ClientChangeGameModePacket(GameMode.SPECTATOR));
        addClientPackets(new ClientChatAckPacket(12549581), new ClientChatAckPacket(Integer.MIN_VALUE), new ClientChatAckPacket(Integer.MAX_VALUE));
        addClientPackets(new ClientCommandChatPacket("l".repeat(256)), new ClientCommandChatPacket("helloworld"));
        //TODO (signed) support signed chat/commands with proper data.
        addClientPackets(new ClientSignedCommandChatPacket("helloworld", Long.MAX_VALUE, 0L, new ArgumentSignatures(List.of(new ArgumentSignatures.Entry("hey", new MessageSignature(new byte[256])))), new LastSeenMessages.Update(100, new BitSet(20)), (byte) 0));
        addClientPackets(new ClientChatMessagePacket("My name is bob", Long.MAX_VALUE, 0L, new MessageSignature(new byte[256]), 100, new BitSet(), (byte) 100));
        //TODO (signed) use a key for tests
        addClientPackets(new ClientChatSessionUpdatePacket(new ChatSession(UUID.randomUUID(), new PlayerPublicKey(Instant.EPOCH, Objects.requireNonNull(MojangCrypt.generateKeyPair()).getPublic(), new byte[4096]))));
        addClientPackets(new ClientChunkBatchReceivedPacket(0.5f));
        addClientPackets(new ClientStatusPacket(ClientStatusPacket.Action.PERFORM_RESPAWN), new ClientStatusPacket(ClientStatusPacket.Action.REQUEST_STATS));
        addClientPackets(new ClientTickEndPacket());
        addClientPackets(new ClientTabCompletePacket(15, "/hellloworld"), new ClientTabCompletePacket(Integer.MIN_VALUE, "/hello arg1 arg2 arg3"), new ClientTabCompletePacket(-1000, "//undo"));
        addClientPackets(new ClientFinishConfigurationPacket());
        addClientPackets(new ClientClickWindowButtonPacket(15, 14), new ClientClickWindowButtonPacket(Integer.MIN_VALUE, Integer.MAX_VALUE));
        addClientPackets(new ClientClickWindowPacket(125, 20, (short) -999, (byte) 1, ClientClickWindowPacket.ClickType.SWAP, Map.of(), ItemStack.Hash.AIR), new ClientClickWindowPacket(Integer.MAX_VALUE, Integer.MIN_VALUE, (short) 51, (byte) 1, ClientClickWindowPacket.ClickType.SWAP, Map.of((short) 5, ItemStack.Hash.AIR), ItemStack.Hash.AIR));
        addClientPackets(new ClientCloseWindowPacket(15), new ClientCloseWindowPacket(Integer.MIN_VALUE));
        addClientPackets(new ClientWindowSlotStatePacket(25, 25, true), new ClientWindowSlotStatePacket(Integer.MAX_VALUE, Integer.MAX_VALUE, true), new ClientWindowSlotStatePacket(Integer.MIN_VALUE, Integer.MAX_VALUE, false));
        //Cookie
        //Plugin message
        addClientPackets(new ClientDebugSubscriptionRequestPacket(Set.of(DebugSubscription.DEDICATED_SERVER_TICK_TIME, DebugSubscription.ENTITY_PATHS)));
        addClientPackets(new ClientEditBookPacket(14, List.of("page1", "page2"), "Wrath of nothing"), new ClientEditBookPacket(15, List.of(), null), new ClientEditBookPacket(12, List.of("hi".repeat(99).split("h")), "What is this book?"));
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
        addClientPackets(new ClientPlayerPositionStatusPacket(true, false), new ClientPlayerPositionStatusPacket(false, false), new ClientPlayerPositionStatusPacket(false, true), new ClientPlayerPositionStatusPacket(true, true));
        addClientPackets(new ClientVehicleMovePacket(new Pos(5, 5, 5, 45f, 45f), true));
        addClientPackets(new ClientVehicleMovePacket(new Pos(6, 5, 6, 82f, 12.5f), false));
        addClientPackets(new ClientSteerBoatPacket(true, false), new ClientSteerBoatPacket(false, false), new ClientSteerBoatPacket(true, true), new ClientSteerBoatPacket(false, true));
        addClientPackets(new ClientPickItemFromBlockPacket(Vec.ONE, true), new ClientPickItemFromBlockPacket(Vec.ZERO, false));
        addClientPackets(new ClientPickItemFromEntityPacket(124, true), new ClientPickItemFromEntityPacket(124, false), new ClientPickItemFromEntityPacket(Integer.MAX_VALUE, true), new ClientPickItemFromEntityPacket(Integer.MIN_VALUE, false));
        addClientPackets(new ClientPlaceRecipePacket((byte) 10, 10, true), new ClientPlaceRecipePacket((byte) 51, 14, false));
        addClientPackets(new ClientPlayerAbilitiesPacket((byte) 0x02));
        addClientPackets(new ClientPlayerDiggingPacket(ClientPlayerDiggingPacket.Status.STARTED_DIGGING, Vec.ZERO, BlockFace.BOTTOM, Integer.MAX_VALUE), new ClientPlayerDiggingPacket(ClientPlayerDiggingPacket.Status.DROP_ITEM_STACK, Vec.ONE, BlockFace.TOP, Integer.MIN_VALUE));
        addClientPackets(new ClientEntityActionPacket(10, ClientEntityActionPacket.Action.LEAVE_BED, 0), new ClientEntityActionPacket(15, ClientEntityActionPacket.Action.START_SPRINTING, 0), new ClientEntityActionPacket(321, ClientEntityActionPacket.Action.START_FLYING_ELYTRA, 0));
        addClientPackets(new ClientInputPacket(true, false, true, false, false, false, true), new ClientInputPacket(false, true, true, false, false, false, true));
        addClientPackets(new ClientPlayerLoadedPacket());
        addClientPackets(new ClientPlayerRotationPacket(45f, 90f, true, false), new ClientPlayerRotationPacket(180f, -45f, false, true));
        addClientPackets(new ClientPlayerBlockPlacementPacket(PlayerHand.MAIN, Vec.ONE, BlockFace.TOP, 0.5f, 0.5f, 0.5f, false, false, 0), new ClientPlayerBlockPlacementPacket(PlayerHand.OFF, Vec.ZERO, BlockFace.BOTTOM, 1f, 1f, 1f, true, true, Integer.MAX_VALUE));
        addClientPackets(new ClientUseItemPacket(PlayerHand.MAIN, 0, 45f, 90f), new ClientUseItemPacket(PlayerHand.OFF, Integer.MAX_VALUE, 180f, -45f));
        addClientPackets(new ClientSpectatePacket(UUID.randomUUID()), new ClientSpectatePacket(new UUID(0, 0)));
        addClientPackets(new ClientSetRecipeBookStatePacket(ClientSetRecipeBookStatePacket.BookType.CRAFTING, true, false), new ClientSetRecipeBookStatePacket(ClientSetRecipeBookStatePacket.BookType.FURNACE, false, true), new ClientSetRecipeBookStatePacket(ClientSetRecipeBookStatePacket.BookType.BLAST_FURNACE, true, true), new ClientSetRecipeBookStatePacket(ClientSetRecipeBookStatePacket.BookType.SMOKER, false, false));
        addClientPackets(new ClientNameItemPacket("Diamond Sword"), new ClientNameItemPacket(""), new ClientNameItemPacket("A".repeat(100)));
        addClientPackets(new ClientResourcePackStatusPacket(UUID.randomUUID(), ResourcePackStatus.ACCEPTED), new ClientResourcePackStatusPacket(UUID.randomUUID(), ResourcePackStatus.DECLINED));
        addClientPackets(new ClientAdvancementTabPacket(AdvancementAction.OPENED_TAB, "minecraft:story/root"), new ClientAdvancementTabPacket(AdvancementAction.CLOSED_SCREEN, null));
        addClientPackets(new ClientSelectTradePacket(0), new ClientSelectTradePacket(5), new ClientSelectTradePacket(Integer.MAX_VALUE));
        addClientPackets(new ClientSetBeaconEffectPacket(PotionType.STRENGTH, PotionType.REGENERATION), new ClientSetBeaconEffectPacket(null, null), new ClientSetBeaconEffectPacket(PotionType.fromKey("strength"), null));
        addClientPackets(new ClientHeldItemChangePacket((short) 0), new ClientHeldItemChangePacket((short) 8), new ClientHeldItemChangePacket((short) 4));
        addClientPackets(new ClientUpdateCommandBlockPacket(Vec.ONE, "/say hello", ClientUpdateCommandBlockPacket.Mode.REDSTONE, (byte) 0), new ClientUpdateCommandBlockPacket(Vec.ZERO, "/tp @p 0 100 0", ClientUpdateCommandBlockPacket.Mode.AUTO, (byte) 0x01));
        addClientPackets(new ClientUpdateCommandBlockMinecartPacket(100, "/say minecart", true), new ClientUpdateCommandBlockMinecartPacket(Integer.MAX_VALUE, "", false));
        addClientPackets(new ClientCreativeInventoryActionPacket((short) 36, ItemStack.of(Material.DIAMOND_SWORD)), new ClientCreativeInventoryActionPacket((short) -1, ItemStack.AIR));
        addClientPackets(new ClientUpdateJigsawBlockPacket(Vec.ONE, "minecraft:village/plains/houses", "minecraft:village/plains/terminators", "minecraft:village/plains/town_centers", "minecraft:air", "rollable", 5, 10));
        addClientPackets(new ClientUpdateStructureBlockPacket(Vec.ZERO, ClientUpdateStructureBlockPacket.Action.UPDATE_DATA, ClientUpdateStructureBlockPacket.Mode.SAVE, "mystructure", new Vec(0, 0, 0), new Vec(10, 10, 10), ClientUpdateStructureBlockPacket.Mirror.NONE, Rotation.NONE, "", 1.0f, 0L, (byte) 0), new ClientUpdateStructureBlockPacket(Vec.ONE, ClientUpdateStructureBlockPacket.Action.SAVE, ClientUpdateStructureBlockPacket.Mode.LOAD, "test", new Vec(5, 5, 5), new Vec(20, 20, 20), ClientUpdateStructureBlockPacket.Mirror.LEFT_RIGHT, Rotation.CLOCKWISE, "metadata", 0.5f, 12345L, ClientUpdateStructureBlockPacket.SHOW_BOUNDING_BOX));
        addClientPackets(new ClientUpdateSignPacket(Vec.ZERO, true, List.of("Line 1", "Line 2", "Line 3", "Line 4")), new ClientUpdateSignPacket(Vec.ONE, false, List.of("", "", "", "")));
        addClientPackets(new ClientAnimationPacket(PlayerHand.MAIN), new ClientAnimationPacket(PlayerHand.OFF));
        addClientPackets(new ClientRecipeBookSeenRecipePacket(0), new ClientRecipeBookSeenRecipePacket(100), new ClientRecipeBookSeenRecipePacket(Integer.MAX_VALUE));
        addClientPackets(new ClientSetTestBlockPacket(Vec.ZERO, ClientSetTestBlockPacket.TestBlockMode.START, "test started"), new ClientSetTestBlockPacket(Vec.ONE, ClientSetTestBlockPacket.TestBlockMode.FAIL, "test failed"), new ClientSetTestBlockPacket(Vec.ZERO, ClientSetTestBlockPacket.TestBlockMode.ACCEPT, ""));
        addClientPackets(new ClientTestInstanceBlockActionPacket(Vec.ZERO, ClientTestInstanceBlockActionPacket.Action.INIT, new ClientTestInstanceBlockActionPacket.Data("mytest", new Vec(10, 10, 10), 0, false, ClientTestInstanceBlockActionPacket.Status.CLEARED, null)), new ClientTestInstanceBlockActionPacket(Vec.ONE, ClientTestInstanceBlockActionPacket.Action.RUN, new ClientTestInstanceBlockActionPacket.Data(null, new Vec(5, 5, 5), 1, true, ClientTestInstanceBlockActionPacket.Status.RUNNING, Component.text("Error!"))));
    }

    private static <T> void testPacket(NetworkBuffer.Type<T> networkType, T packet, Env env) {
        byte[] bytes = NetworkBuffer.makeArray(networkType, packet, env.process());
        var buffer = NetworkBuffer.wrap(bytes, 0, bytes.length, env.process()); // Requires for serialization of some packets
        var createdPacket = buffer.read(networkType);
        assertEquals(packet, createdPacket);
    }

    static <T> Stream<Arguments> packets(PacketParser<T> parser, Map<Class<? extends T>, ? extends Collection<T>> map) {
        return Stream.of(
                        parser.handshake(),
                        parser.status(),
                        parser.login(),
                        parser.configuration(),
                        parser.play()
                ).flatMap(it -> packets(it, map));
    }

    static <T> Stream<Arguments> packets(PacketRegistry<T> registry, Map<Class<? extends T>, ? extends Collection<T>> map) {
        return registry.packets().stream().flatMap(info ->  {
            var tests = map.get(info.packetClass());
            var name = info.packetClass().getSimpleName();
            assertNotNull(tests, "No packet tests for %s".formatted(name));
            assertNotEquals(0, tests.size(), "Empty packet tests for %s".formatted(name));

            var serializer = info.serializer();
            return tests.stream().map(packet ->
                    Arguments.of(serializer, packet)
            );
        });
    }

    static Stream<Arguments> serverPacketArguments() {
        return packets(PacketVanilla.SERVER_PACKET_PARSER, SERVER_PACKETS);
    }

    static Stream<Arguments> clientPacketArguments() {
        return packets(PacketVanilla.CLIENT_PACKET_PARSER, CLIENT_PACKETS);
    }

    @ParameterizedTest(name = "Server Packet Test: {1}")
    @MethodSource("serverPacketArguments")
    void serverPacket(NetworkBuffer.Type<ServerPacket> serializer, ServerPacket packet, Env env) {
        testPacket(serializer, packet, env);
    }

    @ParameterizedTest(name = "Client Packet Test: {1}")
    @MethodSource("clientPacketArguments")
    void clientPacket(NetworkBuffer.Type<ClientPacket> serializer, ClientPacket packet, Env env) {
        testPacket(serializer, packet, env);
    }
}
