package net.minestom.server.network.packet;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
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
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.*;
import net.minestom.server.network.packet.server.configuration.*;
import net.minestom.server.network.packet.server.login.*;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.utils.ObjectPool;
import org.jetbrains.annotations.ApiStatus;

import static java.util.Map.entry;
import static net.minestom.server.network.packet.PacketRegistry.registry;

/**
 * Constants and utilities for vanilla packets.
 */
@ApiStatus.Internal
public final class PacketVanilla {

    /**
     * Pool containing a buffer able to hold the largest packet.
     * <p>
     * Size starts with {@link ServerFlag#POOLED_BUFFER_SIZE} and doubles until {@link ServerFlag#MAX_PACKET_SIZE}.
     */
    public static final ObjectPool<NetworkBuffer> PACKET_POOL = ObjectPool.pool(
            () -> NetworkBuffer.staticBuffer(ServerFlag.POOLED_BUFFER_SIZE, MinecraftServer.process()),
            NetworkBuffer::clear);

    public static ConnectionState nextClientState(ClientPacket packet, ConnectionState currentState) {
        return switch (packet) {
            case ClientHandshakePacket handshakePacket -> switch (handshakePacket.intent()) {
                case STATUS -> ConnectionState.STATUS;
                case LOGIN, TRANSFER -> ConnectionState.LOGIN;
            };
            case ClientLoginAcknowledgedPacket ignored -> ConnectionState.CONFIGURATION;
            case ClientConfigurationAckPacket ignored -> ConnectionState.CONFIGURATION;
            case ClientFinishConfigurationPacket ignored -> ConnectionState.PLAY;
            default -> currentState;
        };
    }

    public static ConnectionState nextServerState(ServerPacket packet, ConnectionState currentState) {
        // Client chooses between STATUS or LOGIN state directly after the first handshake packet
        if (currentState == ConnectionState.HANDSHAKE)
            throw new IllegalStateException("No server Handshake packet exists");
        return switch (packet) {
            case LoginSuccessPacket ignored -> ConnectionState.CONFIGURATION;
            case StartConfigurationPacket ignored -> ConnectionState.CONFIGURATION;
            case FinishConfigurationPacket ignored -> ConnectionState.PLAY;
            default -> currentState;
        };
    }


    static PacketRegistry<ClientPacket.Handshake> CLIENT_HANDSHAKE = registry(ConnectionState.HANDSHAKE, PacketRegistry.ConnectionSide.CLIENT,
            entry(ClientHandshakePacket.class, ClientHandshakePacket.SERIALIZER)
    );

    static PacketRegistry<ClientPacket.Status> CLIENT_STATUS = registry(ConnectionState.STATUS, PacketRegistry.ConnectionSide.CLIENT,
            entry(StatusRequestPacket.class, StatusRequestPacket.SERIALIZER),
            entry(ClientPingRequestPacket.class, ClientPingRequestPacket.SERIALIZER)
    );

    static PacketRegistry<ClientPacket.Login> CLIENT_LOGIN = registry(ConnectionState.LOGIN, PacketRegistry.ConnectionSide.CLIENT,
            entry(ClientLoginStartPacket.class, ClientLoginStartPacket.SERIALIZER),
            entry(ClientEncryptionResponsePacket.class, ClientEncryptionResponsePacket.SERIALIZER),
            entry(ClientLoginPluginResponsePacket.class, ClientLoginPluginResponsePacket.SERIALIZER),
            entry(ClientLoginAcknowledgedPacket.class, ClientLoginAcknowledgedPacket.SERIALIZER),
            entry(ClientCookieResponsePacket.class, ClientCookieResponsePacket.SERIALIZER)
    );

    static PacketRegistry<ClientPacket.Configuration> CLIENT_CONFIGURATION = registry(ConnectionState.CONFIGURATION, PacketRegistry.ConnectionSide.CLIENT,
            entry(ClientSettingsPacket.class, ClientSettingsPacket.SERIALIZER),
            entry(ClientCookieResponsePacket.class, ClientCookieResponsePacket.SERIALIZER),
            entry(ClientPluginMessagePacket.class, ClientPluginMessagePacket.SERIALIZER),
            entry(ClientFinishConfigurationPacket.class, ClientFinishConfigurationPacket.SERIALIZER),
            entry(ClientKeepAlivePacket.class, ClientKeepAlivePacket.SERIALIZER),
            entry(ClientPongPacket.class, ClientPongPacket.SERIALIZER),
            entry(ClientResourcePackStatusPacket.class, ClientResourcePackStatusPacket.SERIALIZER),
            entry(ClientSelectKnownPacksPacket.class, ClientSelectKnownPacksPacket.SERIALIZER),
            entry(ClientCustomClickActionPacket.class, ClientCustomClickActionPacket.SERIALIZER),
            entry(ClientAcceptCodeOfConductPacket.class, ClientAcceptCodeOfConductPacket.SERIALIZER)
    );

    static PacketRegistry<ClientPacket.Play> CLIENT_PLAY = PacketRegistry.<ClientPacket.Play>registry(ConnectionState.PLAY, PacketRegistry.ConnectionSide.CLIENT,
            entry(ClientTeleportConfirmPacket.class, ClientTeleportConfirmPacket.SERIALIZER),
            entry(ClientAttackPacket.class, ClientAttackPacket.SERIALIZER),
            entry(ClientQueryBlockNbtPacket.class, ClientQueryBlockNbtPacket.SERIALIZER),
            entry(ClientSelectBundleItemPacket.class, ClientSelectBundleItemPacket.SERIALIZER),
            entry(ClientChangeDifficultyPacket.class, ClientChangeDifficultyPacket.SERIALIZER),
            entry(ClientChangeGameModePacket.class, ClientChangeGameModePacket.SERIALIZER),
            entry(ClientChatAckPacket.class, ClientChatAckPacket.SERIALIZER),
            entry(ClientCommandChatPacket.class, ClientCommandChatPacket.SERIALIZER),
            entry(ClientSignedCommandChatPacket.class, ClientSignedCommandChatPacket.SERIALIZER),
            entry(ClientChatMessagePacket.class, ClientChatMessagePacket.SERIALIZER),
            entry(ClientChatSessionUpdatePacket.class, ClientChatSessionUpdatePacket.SERIALIZER),
            entry(ClientChunkBatchReceivedPacket.class, ClientChunkBatchReceivedPacket.SERIALIZER),
            entry(ClientStatusPacket.class, ClientStatusPacket.SERIALIZER),
            entry(ClientTickEndPacket.class, ClientTickEndPacket.SERIALIZER),
            entry(ClientSettingsPacket.class, ClientSettingsPacket.SERIALIZER),
            entry(ClientTabCompletePacket.class, ClientTabCompletePacket.SERIALIZER),
            entry(ClientConfigurationAckPacket.class, ClientConfigurationAckPacket.SERIALIZER),
            entry(ClientClickWindowButtonPacket.class, ClientClickWindowButtonPacket.SERIALIZER),
            entry(ClientClickWindowPacket.class, ClientClickWindowPacket.SERIALIZER),
            entry(ClientCloseWindowPacket.class, ClientCloseWindowPacket.SERIALIZER),
            entry(ClientWindowSlotStatePacket.class, ClientWindowSlotStatePacket.SERIALIZER),
            entry(ClientCookieResponsePacket.class, ClientCookieResponsePacket.SERIALIZER),
            entry(ClientPluginMessagePacket.class, ClientPluginMessagePacket.SERIALIZER),
            entry(ClientDebugSubscriptionRequestPacket.class, ClientDebugSubscriptionRequestPacket.SERIALIZER),
            entry(ClientEditBookPacket.class, ClientEditBookPacket.SERIALIZER),
            entry(ClientQueryEntityNbtPacket.class, ClientQueryEntityNbtPacket.SERIALIZER),
            entry(ClientInteractEntityPacket.class, ClientInteractEntityPacket.SERIALIZER),
            entry(ClientGenerateStructurePacket.class, ClientGenerateStructurePacket.SERIALIZER),
            entry(ClientKeepAlivePacket.class, ClientKeepAlivePacket.SERIALIZER),
            entry(ClientLockDifficultyPacket.class, ClientLockDifficultyPacket.SERIALIZER),
            entry(ClientPlayerPositionPacket.class, ClientPlayerPositionPacket.SERIALIZER),
            entry(ClientPlayerPositionAndRotationPacket.class, ClientPlayerPositionAndRotationPacket.SERIALIZER),
            entry(ClientPlayerRotationPacket.class, ClientPlayerRotationPacket.SERIALIZER),
            entry(ClientPlayerPositionStatusPacket.class, ClientPlayerPositionStatusPacket.SERIALIZER),
            entry(ClientVehicleMovePacket.class, ClientVehicleMovePacket.SERIALIZER),
            entry(ClientSteerBoatPacket.class, ClientSteerBoatPacket.SERIALIZER),
            entry(ClientPickItemFromBlockPacket.class, ClientPickItemFromBlockPacket.SERIALIZER),
            entry(ClientPickItemFromEntityPacket.class, ClientPickItemFromEntityPacket.SERIALIZER),
            entry(ClientPingRequestPacket.class, ClientPingRequestPacket.SERIALIZER),
            entry(ClientPlaceRecipePacket.class, ClientPlaceRecipePacket.SERIALIZER),
            entry(ClientPlayerAbilitiesPacket.class, ClientPlayerAbilitiesPacket.SERIALIZER),
            entry(ClientPlayerActionPacket.class, ClientPlayerActionPacket.SERIALIZER),
            entry(ClientEntityActionPacket.class, ClientEntityActionPacket.SERIALIZER),
            entry(ClientInputPacket.class, ClientInputPacket.SERIALIZER),
            entry(ClientPlayerLoadedPacket.class, ClientPlayerLoadedPacket.SERIALIZER),
            entry(ClientPongPacket.class, ClientPongPacket.SERIALIZER),
            entry(ClientSetRecipeBookStatePacket.class, ClientSetRecipeBookStatePacket.SERIALIZER),
            entry(ClientRecipeBookSeenRecipePacket.class, ClientRecipeBookSeenRecipePacket.SERIALIZER),
            entry(ClientNameItemPacket.class, ClientNameItemPacket.SERIALIZER),
            entry(ClientResourcePackStatusPacket.class, ClientResourcePackStatusPacket.SERIALIZER),
            entry(ClientAdvancementTabPacket.class, ClientAdvancementTabPacket.SERIALIZER),
            entry(ClientSelectTradePacket.class, ClientSelectTradePacket.SERIALIZER),
            entry(ClientSetBeaconEffectPacket.class, ClientSetBeaconEffectPacket.SERIALIZER),
            entry(ClientHeldItemChangePacket.class, ClientHeldItemChangePacket.SERIALIZER),
            entry(ClientUpdateCommandBlockPacket.class, ClientUpdateCommandBlockPacket.SERIALIZER),
            entry(ClientUpdateCommandBlockMinecartPacket.class, ClientUpdateCommandBlockMinecartPacket.SERIALIZER),
            entry(ClientCreativeInventoryActionPacket.class, ClientCreativeInventoryActionPacket.SERIALIZER),
            entry(ClientSetGameRulesPacket.class, ClientSetGameRulesPacket.SERIALIZER),
            entry(ClientUpdateJigsawBlockPacket.class, ClientUpdateJigsawBlockPacket.SERIALIZER),
            entry(ClientUpdateStructureBlockPacket.class, ClientUpdateStructureBlockPacket.SERIALIZER),
            entry(ClientSetTestBlockPacket.class, ClientSetTestBlockPacket.SERIALIZER),
            entry(ClientUpdateSignPacket.class, ClientUpdateSignPacket.SERIALIZER),
            entry(ClientSpectatorActionPacket.class, ClientSpectatorActionPacket.SERIALIZER),
            entry(ClientAnimationPacket.class, ClientAnimationPacket.SERIALIZER),
            entry(ClientTeleportToEntityPacket.class, ClientTeleportToEntityPacket.SERIALIZER),
            entry(ClientTestInstanceBlockActionPacket.class, ClientTestInstanceBlockActionPacket.SERIALIZER),
            entry(ClientPlayerBlockPlacementPacket.class, ClientPlayerBlockPlacementPacket.SERIALIZER),
            entry(ClientUseItemPacket.class, ClientUseItemPacket.SERIALIZER),
            entry(ClientCustomClickActionPacket.class, ClientCustomClickActionPacket.SERIALIZER)
    );

    static PacketRegistry<ServerPacket.Handshake> SERVER_HANDSHAKE = registry(ConnectionState.HANDSHAKE, PacketRegistry.ConnectionSide.SERVER);

    static PacketRegistry<ServerPacket.Status> SERVER_STATUS = registry(ConnectionState.STATUS, PacketRegistry.ConnectionSide.SERVER,
            entry(ResponsePacket.class, ResponsePacket.SERIALIZER),
            entry(PingResponsePacket.class, PingResponsePacket.SERIALIZER)
    );

    static PacketRegistry<ServerPacket.Login> SERVER_LOGIN = registry(ConnectionState.LOGIN, PacketRegistry.ConnectionSide.SERVER,
            entry(LoginDisconnectPacket.class, LoginDisconnectPacket.SERIALIZER),
            entry(EncryptionRequestPacket.class, EncryptionRequestPacket.SERIALIZER),
            entry(LoginSuccessPacket.class, LoginSuccessPacket.SERIALIZER),
            entry(SetCompressionPacket.class, SetCompressionPacket.SERIALIZER),
            entry(LoginPluginRequestPacket.class, LoginPluginRequestPacket.SERIALIZER),
            entry(CookieRequestPacket.class, CookieRequestPacket.SERIALIZER)
    );

    static PacketRegistry<ServerPacket.Configuration> SERVER_CONFIGURATION = registry(ConnectionState.CONFIGURATION, PacketRegistry.ConnectionSide.SERVER,
            entry(CookieRequestPacket.class, CookieRequestPacket.SERIALIZER),
            entry(PluginMessagePacket.class, PluginMessagePacket.SERIALIZER),
            entry(DisconnectPacket.class, DisconnectPacket.SERIALIZER),
            entry(FinishConfigurationPacket.class, FinishConfigurationPacket.SERIALIZER),
            entry(KeepAlivePacket.class, KeepAlivePacket.SERIALIZER),
            entry(PingPacket.class, PingPacket.SERIALIZER),
            entry(ResetChatPacket.class, ResetChatPacket.SERIALIZER),
            entry(RegistryDataPacket.class, RegistryDataPacket.SERIALIZER),
            entry(ResourcePackPopPacket.class, ResourcePackPopPacket.SERIALIZER),
            entry(ResourcePackPushPacket.class, ResourcePackPushPacket.SERIALIZER),
            entry(CookieStorePacket.class, CookieStorePacket.SERIALIZER),
            entry(TransferPacket.class, TransferPacket.SERIALIZER),
            entry(UpdateEnabledFeaturesPacket.class, UpdateEnabledFeaturesPacket.SERIALIZER),
            entry(TagsPacket.class, TagsPacket.SERIALIZER),
            entry(SelectKnownPacksPacket.class, SelectKnownPacksPacket.SERIALIZER),
            entry(CustomReportDetailsPacket.class, CustomReportDetailsPacket.SERIALIZER),
            entry(ServerLinksPacket.class, ServerLinksPacket.SERIALIZER),
            entry(ClearDialogPacket.class, ClearDialogPacket.SERIALIZER),
            entry(ShowDialogPacket.class, ShowDialogPacket.INLINE_SERIALIZER),
            entry(CodeOfConductPacket.class, CodeOfConductPacket.SERIALIZER)
    );

    static PacketRegistry<ServerPacket.Play> SERVER_PLAY = PacketRegistry.<ServerPacket.Play>registry(ConnectionState.PLAY, PacketRegistry.ConnectionSide.SERVER,
            entry(BundlePacket.class, BundlePacket.SERIALIZER),
            entry(SpawnEntityPacket.class, SpawnEntityPacket.SERIALIZER),
            entry(EntityAnimationPacket.class, EntityAnimationPacket.SERIALIZER),
            entry(StatisticsPacket.class, StatisticsPacket.SERIALIZER),
            entry(AcknowledgeBlockChangePacket.class, AcknowledgeBlockChangePacket.SERIALIZER),
            entry(BlockBreakAnimationPacket.class, BlockBreakAnimationPacket.SERIALIZER),
            entry(BlockEntityDataPacket.class, BlockEntityDataPacket.SERIALIZER),
            entry(BlockActionPacket.class, BlockActionPacket.SERIALIZER),
            entry(BlockChangePacket.class, BlockChangePacket.SERIALIZER),
            entry(BossBarPacket.class, BossBarPacket.SERIALIZER),
            entry(ServerDifficultyPacket.class, ServerDifficultyPacket.SERIALIZER),
            entry(ChunkBatchFinishedPacket.class, ChunkBatchFinishedPacket.SERIALIZER),
            entry(ChunkBatchStartPacket.class, ChunkBatchStartPacket.SERIALIZER),
            entry(ChunkBiomesPacket.class, ChunkBiomesPacket.SERIALIZER),
            entry(ClearTitlesPacket.class, ClearTitlesPacket.SERIALIZER),
            entry(TabCompletePacket.class, TabCompletePacket.SERIALIZER),
            entry(DeclareCommandsPacket.class, DeclareCommandsPacket.SERIALIZER),
            entry(CloseWindowPacket.class, CloseWindowPacket.SERIALIZER),
            entry(WindowItemsPacket.class, WindowItemsPacket.SERIALIZER),
            entry(WindowPropertyPacket.class, WindowPropertyPacket.SERIALIZER),
            entry(SetSlotPacket.class, SetSlotPacket.SERIALIZER),
            entry(CookieRequestPacket.class, CookieRequestPacket.SERIALIZER),
            entry(SetCooldownPacket.class, SetCooldownPacket.SERIALIZER),
            entry(CustomChatCompletionPacket.class, CustomChatCompletionPacket.SERIALIZER),
            entry(PluginMessagePacket.class, PluginMessagePacket.SERIALIZER),
            entry(DamageEventPacket.class, DamageEventPacket.SERIALIZER),
            entry(DebugBlockValuePacket.class, DebugBlockValuePacket.SERIALIZER),
            entry(DebugChunkValuePacket.class, DebugChunkValuePacket.SERIALIZER),
            entry(DebugEntityValuePacket.class, DebugEntityValuePacket.SERIALIZER),
            entry(DebugEventPacket.class, DebugEventPacket.SERIALIZER),
            entry(DebugSamplePacket.class, DebugSamplePacket.SERIALIZER),
            entry(DeleteChatPacket.class, DeleteChatPacket.SERIALIZER),
            entry(DisconnectPacket.class, DisconnectPacket.SERIALIZER),
            entry(DisguisedChatPacket.class, DisguisedChatPacket.SERIALIZER),
            entry(EntityStatusPacket.class, EntityStatusPacket.SERIALIZER),
            entry(EntityPositionSyncPacket.class, EntityPositionSyncPacket.SERIALIZER),
            entry(ExplosionPacket.class, ExplosionPacket.SERIALIZER),
            entry(UnloadChunkPacket.class, UnloadChunkPacket.SERIALIZER),
            entry(ChangeGameStatePacket.class, ChangeGameStatePacket.SERIALIZER),
            entry(GameRuleValuesPacket.class, GameRuleValuesPacket.SERIALIZER),
            entry(GameTestHighlightPosPacket.class, GameTestHighlightPosPacket.SERIALIZER),
            entry(OpenHorseWindowPacket.class, OpenHorseWindowPacket.SERIALIZER),
            entry(HitAnimationPacket.class, HitAnimationPacket.SERIALIZER),
            entry(InitializeWorldBorderPacket.class, InitializeWorldBorderPacket.SERIALIZER),
            entry(KeepAlivePacket.class, KeepAlivePacket.SERIALIZER),
            entry(ChunkDataPacket.class, ChunkDataPacket.SERIALIZER),
            entry(WorldEventPacket.class, WorldEventPacket.SERIALIZER),
            entry(ParticlePacket.class, ParticlePacket.SERIALIZER),
            entry(UpdateLightPacket.class, UpdateLightPacket.SERIALIZER),
            entry(JoinGamePacket.class, JoinGamePacket.SERIALIZER),
            entry(LowDiskSpaceWarningPacket.class, LowDiskSpaceWarningPacket.SERIALIZER),
            entry(MapDataPacket.class, MapDataPacket.SERIALIZER),
            entry(TradeListPacket.class, TradeListPacket.SERIALIZER),
            entry(EntityPositionPacket.class, EntityPositionPacket.SERIALIZER),
            entry(EntityPositionAndRotationPacket.class, EntityPositionAndRotationPacket.SERIALIZER),
            entry(MoveMinecartPacket.class, MoveMinecartPacket.SERIALIZER),
            entry(EntityRotationPacket.class, EntityRotationPacket.SERIALIZER),
            entry(VehicleMovePacket.class, VehicleMovePacket.SERIALIZER),
            entry(OpenBookPacket.class, OpenBookPacket.SERIALIZER),
            entry(OpenWindowPacket.class, OpenWindowPacket.SERIALIZER),
            entry(OpenSignEditorPacket.class, OpenSignEditorPacket.SERIALIZER),
            entry(PingPacket.class, PingPacket.SERIALIZER),
            entry(PingResponsePacket.class, PingResponsePacket.SERIALIZER),
            entry(PlaceGhostRecipePacket.class, PlaceGhostRecipePacket.SERIALIZER),
            entry(PlayerAbilitiesPacket.class, PlayerAbilitiesPacket.SERIALIZER),
            entry(PlayerChatMessagePacket.class, PlayerChatMessagePacket.SERIALIZER),
            entry(EndCombatEventPacket.class, EndCombatEventPacket.SERIALIZER),
            entry(EnterCombatEventPacket.class, EnterCombatEventPacket.SERIALIZER),
            entry(DeathCombatEventPacket.class, DeathCombatEventPacket.SERIALIZER),
            entry(PlayerInfoRemovePacket.class, PlayerInfoRemovePacket.SERIALIZER),
            entry(PlayerInfoUpdatePacket.class, PlayerInfoUpdatePacket.SERIALIZER),
            entry(FacePlayerPacket.class, FacePlayerPacket.SERIALIZER),
            entry(PlayerPositionAndLookPacket.class, PlayerPositionAndLookPacket.SERIALIZER),
            entry(PlayerRotationPacket.class, PlayerRotationPacket.SERIALIZER),
            entry(RecipeBookAddPacket.class, RecipeBookAddPacket.SERIALIZER),
            entry(RecipeBookRemovePacket.class, RecipeBookRemovePacket.SERIALIZER),
            entry(RecipeBookSettingsPacket.class, RecipeBookSettingsPacket.SERIALIZER),
            entry(DestroyEntitiesPacket.class, DestroyEntitiesPacket.SERIALIZER),
            entry(RemoveEntityEffectPacket.class, RemoveEntityEffectPacket.SERIALIZER),
            entry(ResetScorePacket.class, ResetScorePacket.SERIALIZER),
            entry(ResourcePackPopPacket.class, ResourcePackPopPacket.SERIALIZER),
            entry(ResourcePackPushPacket.class, ResourcePackPushPacket.SERIALIZER),
            entry(RespawnPacket.class, RespawnPacket.SERIALIZER),
            entry(EntityHeadLookPacket.class, EntityHeadLookPacket.SERIALIZER),
            entry(MultiBlockChangePacket.class, MultiBlockChangePacket.SERIALIZER),
            entry(SelectAdvancementTabPacket.class, SelectAdvancementTabPacket.SERIALIZER),
            entry(ServerDataPacket.class, ServerDataPacket.SERIALIZER),
            entry(ActionBarPacket.class, ActionBarPacket.SERIALIZER),
            entry(WorldBorderCenterPacket.class, WorldBorderCenterPacket.SERIALIZER),
            entry(WorldBorderLerpSizePacket.class, WorldBorderLerpSizePacket.SERIALIZER),
            entry(WorldBorderSizePacket.class, WorldBorderSizePacket.SERIALIZER),
            entry(WorldBorderWarningDelayPacket.class, WorldBorderWarningDelayPacket.SERIALIZER),
            entry(WorldBorderWarningReachPacket.class, WorldBorderWarningReachPacket.SERIALIZER),
            entry(CameraPacket.class, CameraPacket.SERIALIZER),
            entry(UpdateViewPositionPacket.class, UpdateViewPositionPacket.SERIALIZER),
            entry(UpdateViewDistancePacket.class, UpdateViewDistancePacket.SERIALIZER),
            entry(SetCursorItemPacket.class, SetCursorItemPacket.SERIALIZER),
            entry(SpawnPositionPacket.class, SpawnPositionPacket.SERIALIZER),
            entry(DisplayScoreboardPacket.class, DisplayScoreboardPacket.SERIALIZER),
            entry(EntityMetaDataPacket.class, EntityMetaDataPacket.SERIALIZER),
            entry(AttachEntityPacket.class, AttachEntityPacket.SERIALIZER),
            entry(EntityVelocityPacket.class, EntityVelocityPacket.SERIALIZER),
            entry(EntityEquipmentPacket.class, EntityEquipmentPacket.SERIALIZER),
            entry(SetExperiencePacket.class, SetExperiencePacket.SERIALIZER),
            entry(UpdateHealthPacket.class, UpdateHealthPacket.SERIALIZER),
            entry(HeldItemChangePacket.class, HeldItemChangePacket.SERIALIZER),
            entry(ScoreboardObjectivePacket.class, ScoreboardObjectivePacket.SERIALIZER),
            entry(SetPassengersPacket.class, SetPassengersPacket.SERIALIZER),
            entry(SetPlayerInventorySlotPacket.class, SetPlayerInventorySlotPacket.SERIALIZER),
            entry(TeamsPacket.class, TeamsPacket.SERIALIZER),
            entry(UpdateScorePacket.class, UpdateScorePacket.SERIALIZER),
            entry(UpdateSimulationDistancePacket.class, UpdateSimulationDistancePacket.SERIALIZER),
            entry(SetTitleSubTitlePacket.class, SetTitleSubTitlePacket.SERIALIZER),
            entry(SetTimePacket.class, SetTimePacket.SERIALIZER),
            entry(SetTitleTextPacket.class, SetTitleTextPacket.SERIALIZER),
            entry(SetTitleTimePacket.class, SetTitleTimePacket.SERIALIZER),
            entry(EntitySoundEffectPacket.class, EntitySoundEffectPacket.SERIALIZER),
            entry(SoundEffectPacket.class, SoundEffectPacket.SERIALIZER),
            entry(StartConfigurationPacket.class, StartConfigurationPacket.SERIALIZER),
            entry(StopSoundPacket.class, StopSoundPacket.SERIALIZER),
            entry(CookieStorePacket.class, CookieStorePacket.SERIALIZER),
            entry(SystemChatPacket.class, SystemChatPacket.SERIALIZER),
            entry(PlayerListHeaderAndFooterPacket.class, PlayerListHeaderAndFooterPacket.SERIALIZER),
            entry(NbtQueryResponsePacket.class, NbtQueryResponsePacket.SERIALIZER),
            entry(CollectItemPacket.class, CollectItemPacket.SERIALIZER),
            entry(EntityTeleportPacket.class, EntityTeleportPacket.SERIALIZER),
            entry(TestInstanceBlockStatus.class, TestInstanceBlockStatus.SERIALIZER),
            entry(SetTickStatePacket.class, SetTickStatePacket.SERIALIZER),
            entry(TickStepPacket.class, TickStepPacket.SERIALIZER),
            entry(TransferPacket.class, TransferPacket.SERIALIZER),
            entry(AdvancementsPacket.class, AdvancementsPacket.SERIALIZER),
            entry(EntityAttributesPacket.class, EntityAttributesPacket.SERIALIZER),
            entry(EntityEffectPacket.class, EntityEffectPacket.SERIALIZER),
            entry(DeclareRecipesPacket.class, DeclareRecipesPacket.SERIALIZER),
            entry(TagsPacket.class, TagsPacket.SERIALIZER),
            entry(ProjectilePowerPacket.class, ProjectilePowerPacket.SERIALIZER),
            entry(CustomReportDetailsPacket.class, CustomReportDetailsPacket.SERIALIZER),
            entry(ServerLinksPacket.class, ServerLinksPacket.SERIALIZER),
            entry(TrackedWaypointPacket.class, TrackedWaypointPacket.SERIALIZER),
            entry(ClearDialogPacket.class, ClearDialogPacket.SERIALIZER),
            entry(ShowDialogPacket.class, ShowDialogPacket.SERIALIZER)
    );

    public static final PacketParser.Client CLIENT_PACKET_PARSER = new PacketParser.Client(
            CLIENT_HANDSHAKE,
            CLIENT_STATUS,
            CLIENT_LOGIN,
            CLIENT_CONFIGURATION,
            CLIENT_PLAY
    );
    public static final PacketParser.Server SERVER_PACKET_PARSER = new PacketParser.Server(
            SERVER_HANDSHAKE,
            SERVER_STATUS,
            SERVER_LOGIN,
            SERVER_CONFIGURATION,
            SERVER_PLAY
    );
}
