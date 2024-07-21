package net.minestom.server.network.packet;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.*;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.configuration.ClientSelectKnownPacksPacket;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public interface PacketRegistry<T> {
    @UnknownNullability T create(int packetId, @NotNull NetworkBuffer reader);

    int packetId(@NotNull Class<? extends T> packetClass);

    sealed class Client extends PacketRegistryTemplate<ClientPacket> {
        @SafeVarargs
        Client(Entry<ClientPacket>... suppliers) {
            super(suppliers);
        }
    }

    final class ClientStatus extends Client {
        public ClientStatus() {
            super(
                    entry(StatusRequestPacket.class, StatusRequestPacket::new),
                    entry(ClientPingRequestPacket.class, ClientPingRequestPacket::new)
            );
        }
    }

    final class ClientLogin extends Client {
        public ClientLogin() {
            super(
                    entry(ClientLoginStartPacket.class, ClientLoginStartPacket::new),
                    entry(ClientEncryptionResponsePacket.class, ClientEncryptionResponsePacket::new),
                    entry(ClientLoginPluginResponsePacket.class, ClientLoginPluginResponsePacket::new),
                    entry(ClientLoginAcknowledgedPacket.class, ClientLoginAcknowledgedPacket::new),
                    entry(ClientCookieResponsePacket.class, ClientCookieResponsePacket::new)
            );
        }
    }

    final class ClientConfiguration extends Client {
        public ClientConfiguration() {
            super(
                    entry(ClientSettingsPacket.class, ClientSettingsPacket::new),
                    entry(ClientCookieResponsePacket.class, ClientCookieResponsePacket::new),
                    entry(ClientPluginMessagePacket.class, ClientPluginMessagePacket::new),
                    entry(ClientFinishConfigurationPacket.class, ClientFinishConfigurationPacket::new),
                    entry(ClientKeepAlivePacket.class, ClientKeepAlivePacket::new),
                    entry(ClientPongPacket.class, ClientPongPacket::new),
                    entry(ClientResourcePackStatusPacket.class, ClientResourcePackStatusPacket::new),
                    entry(ClientSelectKnownPacksPacket.class, ClientSelectKnownPacksPacket::new)
            );
        }
    }

    final class ClientPlay extends Client {
        public ClientPlay() {
            super(
                    entry(ClientTeleportConfirmPacket.class, ClientTeleportConfirmPacket::new),
                    entry(ClientQueryBlockNbtPacket.class, ClientQueryBlockNbtPacket::new),
                    null, // difficulty packet
                    entry(ClientChatAckPacket.class, ClientChatAckPacket::new),
                    entry(ClientCommandChatPacket.class, ClientCommandChatPacket::new),
                    entry(ClientSignedCommandChatPacket.class, ClientSignedCommandChatPacket::new),
                    entry(ClientChatMessagePacket.class, ClientChatMessagePacket::new),
                    entry(ClientChatSessionUpdatePacket.class, ClientChatSessionUpdatePacket::new),
                    entry(ClientChunkBatchReceivedPacket.class, ClientChunkBatchReceivedPacket::new),
                    entry(ClientStatusPacket.class, ClientStatusPacket::new),
                    entry(ClientSettingsPacket.class, ClientSettingsPacket::new),
                    entry(ClientTabCompletePacket.class, ClientTabCompletePacket::new),
                    entry(ClientConfigurationAckPacket.class, ClientConfigurationAckPacket::new),
                    entry(ClientClickWindowButtonPacket.class, ClientClickWindowButtonPacket::new),
                    entry(ClientClickWindowPacket.class, ClientClickWindowPacket::new),
                    entry(ClientCloseWindowPacket.class, ClientCloseWindowPacket::new),
                    entry(ClientWindowSlotStatePacket.class, ClientWindowSlotStatePacket::new),
                    entry(ClientCookieResponsePacket.class, ClientCookieResponsePacket::new),
                    entry(ClientPluginMessagePacket.class, ClientPluginMessagePacket::new),
                    entry(ClientDebugSampleSubscriptionPacket.class, ClientDebugSampleSubscriptionPacket::new),
                    entry(ClientEditBookPacket.class, ClientEditBookPacket::new),
                    entry(ClientQueryEntityNbtPacket.class, ClientQueryEntityNbtPacket::new),
                    entry(ClientInteractEntityPacket.class, ClientInteractEntityPacket::new),
                    entry(ClientGenerateStructurePacket.class, ClientGenerateStructurePacket::new),
                    entry(ClientKeepAlivePacket.class, ClientKeepAlivePacket::new),
                    null, // lock difficulty
                    entry(ClientPlayerPositionPacket.class, ClientPlayerPositionPacket::new),
                    entry(ClientPlayerPositionAndRotationPacket.class, ClientPlayerPositionAndRotationPacket::new),
                    entry(ClientPlayerRotationPacket.class, ClientPlayerRotationPacket::new),
                    entry(ClientPlayerPacket.class, ClientPlayerPacket::new),
                    entry(ClientVehicleMovePacket.class, ClientVehicleMovePacket::new),
                    entry(ClientSteerBoatPacket.class, ClientSteerBoatPacket::new),
                    entry(ClientPickItemPacket.class, ClientPickItemPacket::new),
                    entry(ClientPingRequestPacket.class, ClientPingRequestPacket::new),
                    entry(ClientCraftRecipeRequest.class, ClientCraftRecipeRequest::new),
                    entry(ClientPlayerAbilitiesPacket.class, ClientPlayerAbilitiesPacket::new),
                    entry(ClientPlayerDiggingPacket.class, ClientPlayerDiggingPacket::new),
                    entry(ClientEntityActionPacket.class, ClientEntityActionPacket::new),
                    entry(ClientSteerVehiclePacket.class, ClientSteerVehiclePacket::new),
                    entry(ClientPongPacket.class, ClientPongPacket::new),
                    entry(ClientSetRecipeBookStatePacket.class, ClientSetRecipeBookStatePacket::new),
                    entry(ClientSetDisplayedRecipePacket.class, ClientSetDisplayedRecipePacket::new),
                    entry(ClientNameItemPacket.class, ClientNameItemPacket::new),
                    entry(ClientResourcePackStatusPacket.class, ClientResourcePackStatusPacket::new),
                    entry(ClientAdvancementTabPacket.class, ClientAdvancementTabPacket::new),
                    entry(ClientSelectTradePacket.class, ClientSelectTradePacket::new),
                    entry(ClientSetBeaconEffectPacket.class, ClientSetBeaconEffectPacket::new),
                    entry(ClientHeldItemChangePacket.class, ClientHeldItemChangePacket::new),
                    entry(ClientUpdateCommandBlockPacket.class, ClientUpdateCommandBlockPacket::new),
                    entry(ClientUpdateCommandBlockMinecartPacket.class, ClientUpdateCommandBlockMinecartPacket::new),
                    entry(ClientCreativeInventoryActionPacket.class, ClientCreativeInventoryActionPacket::new),
                    null, // Update Jigsaw Block
                    entry(ClientUpdateStructureBlockPacket.class, ClientUpdateStructureBlockPacket::new),
                    entry(ClientUpdateSignPacket.class, ClientUpdateSignPacket::new),
                    entry(ClientAnimationPacket.class, ClientAnimationPacket::new),
                    entry(ClientSpectatePacket.class, ClientSpectatePacket::new),
                    entry(ClientPlayerBlockPlacementPacket.class, ClientPlayerBlockPlacementPacket::new),
                    entry(ClientUseItemPacket.class, ClientUseItemPacket::new)
            );
        }
    }

    sealed class Server extends PacketRegistryTemplate<ServerPacket> {
        @SafeVarargs
        Server(Entry<ServerPacket>... suppliers) {
            super(suppliers);
        }
    }

    final class ServerStatus extends Server {
        public ServerStatus() {
            super(
                    entry(ResponsePacket.class, ResponsePacket::new),
                    entry(PingResponsePacket.class, PingResponsePacket::new)
            );
        }
    }

    final class ServerLogin extends Server {
        public ServerLogin() {
            super(
                    entry(LoginDisconnectPacket.class, LoginDisconnectPacket::new),
                    entry(EncryptionRequestPacket.class, EncryptionRequestPacket::new),
                    entry(LoginSuccessPacket.class, LoginSuccessPacket::new),
                    entry(SetCompressionPacket.class, SetCompressionPacket::new),
                    entry(LoginPluginRequestPacket.class, LoginPluginRequestPacket::new),
                    entry(CookieRequestPacket.class, CookieRequestPacket::new)
            );
        }
    }

    final class ServerConfiguration extends Server {
        public ServerConfiguration() {
            super(
                    entry(CookieRequestPacket.class, CookieRequestPacket::new),
                    entry(PluginMessagePacket.class, PluginMessagePacket::new),
                    entry(DisconnectPacket.class, DisconnectPacket::new),
                    entry(FinishConfigurationPacket.class, FinishConfigurationPacket::new),
                    entry(KeepAlivePacket.class, KeepAlivePacket::new),
                    entry(PingPacket.class, PingPacket::new),
                    entry(ResetChatPacket.class, ResetChatPacket::new),
                    entry(RegistryDataPacket.class, RegistryDataPacket::new),
                    entry(ResourcePackPopPacket.class, ResourcePackPopPacket::new),
                    entry(ResourcePackPushPacket.class, ResourcePackPushPacket::new),
                    entry(CookieStorePacket.class, CookieStorePacket::new),
                    entry(TransferPacket.class, TransferPacket::new),
                    entry(UpdateEnabledFeaturesPacket.class, UpdateEnabledFeaturesPacket::new),
                    entry(TagsPacket.class, TagsPacket::new),
                    entry(SelectKnownPacksPacket.class, SelectKnownPacksPacket::new),
                    entry(CustomReportDetailsPacket.class, CustomReportDetailsPacket::new),
                    entry(ServerLinksPacket.class, ServerLinksPacket::new)
            );
        }
    }

    final class ServerPlay extends Server {
        public ServerPlay() {
            super(
                    entry(BundlePacket.class, BundlePacket::new),
                    entry(SpawnEntityPacket.class, SpawnEntityPacket::new),
                    entry(SpawnExperienceOrbPacket.class, SpawnExperienceOrbPacket::new),
                    entry(EntityAnimationPacket.class, EntityAnimationPacket::new),
                    entry(StatisticsPacket.class, StatisticsPacket::new),
                    entry(AcknowledgeBlockChangePacket.class, AcknowledgeBlockChangePacket::new),
                    entry(BlockBreakAnimationPacket.class, BlockBreakAnimationPacket::new),
                    entry(BlockEntityDataPacket.class, BlockEntityDataPacket::new),
                    entry(BlockActionPacket.class, BlockActionPacket::new),
                    entry(BlockChangePacket.class, BlockChangePacket::new),
                    entry(BossBarPacket.class, BossBarPacket::new),
                    entry(ServerDifficultyPacket.class, ServerDifficultyPacket::new),
                    entry(ChunkBatchFinishedPacket.class, ChunkBatchFinishedPacket::new),
                    entry(ChunkBatchStartPacket.class, ChunkBatchStartPacket::new),
                    null, // CHUNK_BIOMES
                    entry(ClearTitlesPacket.class, ClearTitlesPacket::new),
                    entry(TabCompletePacket.class, TabCompletePacket::new),
                    entry(DeclareCommandsPacket.class, DeclareCommandsPacket::new),
                    entry(CloseWindowPacket.class, CloseWindowPacket::new),
                    entry(WindowItemsPacket.class, WindowItemsPacket::new),
                    entry(WindowPropertyPacket.class, WindowPropertyPacket::new),
                    entry(SetSlotPacket.class, SetSlotPacket::new),
                    entry(CookieRequestPacket.class, CookieRequestPacket::new),
                    entry(SetCooldownPacket.class, SetCooldownPacket::new),
                    entry(CustomChatCompletionPacket.class, CustomChatCompletionPacket::new),
                    entry(PluginMessagePacket.class, PluginMessagePacket::new),
                    entry(DamageEventPacket.class, DamageEventPacket::new),
                    entry(DebugSamplePacket.class, DebugSamplePacket::new),
                    entry(DeleteChatPacket.class, DeleteChatPacket::new),
                    entry(DisconnectPacket.class, DisconnectPacket::new),
                    null, // DISGUISED_CHAT
                    entry(EntityStatusPacket.class, EntityStatusPacket::new),
                    entry(ExplosionPacket.class, ExplosionPacket::new),
                    entry(UnloadChunkPacket.class, UnloadChunkPacket::new),
                    entry(ChangeGameStatePacket.class, ChangeGameStatePacket::new),
                    entry(OpenHorseWindowPacket.class, OpenHorseWindowPacket::new),
                    entry(HitAnimationPacket.class, HitAnimationPacket::new),
                    entry(InitializeWorldBorderPacket.class, InitializeWorldBorderPacket::new),
                    entry(KeepAlivePacket.class, KeepAlivePacket::new),
                    entry(ChunkDataPacket.class, ChunkDataPacket::new),
                    entry(EffectPacket.class, EffectPacket::new),
                    entry(ParticlePacket.class, ParticlePacket::new),
                    entry(UpdateLightPacket.class, UpdateLightPacket::new),
                    entry(JoinGamePacket.class, JoinGamePacket::new),
                    entry(MapDataPacket.class, MapDataPacket::new),
                    entry(TradeListPacket.class, TradeListPacket::new),
                    entry(EntityPositionPacket.class, EntityPositionPacket::new),
                    entry(EntityPositionAndRotationPacket.class, EntityPositionAndRotationPacket::new),
                    entry(EntityRotationPacket.class, EntityRotationPacket::new),
                    entry(VehicleMovePacket.class, VehicleMovePacket::new),
                    entry(OpenBookPacket.class, OpenBookPacket::new),
                    entry(OpenWindowPacket.class, OpenWindowPacket::new),
                    entry(OpenSignEditorPacket.class, OpenSignEditorPacket::new),
                    entry(PingPacket.class, PingPacket::new),
                    entry(PingResponsePacket.class, PingResponsePacket::new),
                    entry(CraftRecipeResponse.class, CraftRecipeResponse::new),
                    entry(PlayerAbilitiesPacket.class, PlayerAbilitiesPacket::new),
                    entry(PlayerChatMessagePacket.class, PlayerChatMessagePacket::new),
                    entry(EndCombatEventPacket.class, EndCombatEventPacket::new),
                    entry(EnterCombatEventPacket.class, EnterCombatEventPacket::new),
                    entry(DeathCombatEventPacket.class, DeathCombatEventPacket::new),
                    entry(PlayerInfoRemovePacket.class, PlayerInfoRemovePacket::new),
                    entry(PlayerInfoUpdatePacket.class, PlayerInfoUpdatePacket::new),
                    entry(FacePlayerPacket.class, FacePlayerPacket::new),
                    entry(PlayerPositionAndLookPacket.class, PlayerPositionAndLookPacket::new),
                    entry(UnlockRecipesPacket.class, UnlockRecipesPacket::new),
                    entry(DestroyEntitiesPacket.class, DestroyEntitiesPacket::new),
                    entry(RemoveEntityEffectPacket.class, RemoveEntityEffectPacket::new),
                    entry(ResetScorePacket.class, ResetScorePacket::new),
                    entry(ResourcePackPopPacket.class, ResourcePackPopPacket::new),
                    entry(ResourcePackPushPacket.class, ResourcePackPushPacket::new),
                    entry(RespawnPacket.class, RespawnPacket::new),
                    entry(EntityHeadLookPacket.class, EntityHeadLookPacket::new),
                    entry(MultiBlockChangePacket.class, MultiBlockChangePacket::new),
                    entry(SelectAdvancementTabPacket.class, SelectAdvancementTabPacket::new),
                    entry(ServerDataPacket.class, ServerDataPacket::new),
                    entry(ActionBarPacket.class, ActionBarPacket::new),
                    entry(WorldBorderCenterPacket.class, WorldBorderCenterPacket::new),
                    entry(WorldBorderLerpSizePacket.class, WorldBorderLerpSizePacket::new),
                    entry(WorldBorderSizePacket.class, WorldBorderSizePacket::new),
                    entry(WorldBorderWarningDelayPacket.class, WorldBorderWarningDelayPacket::new),
                    entry(WorldBorderWarningReachPacket.class, WorldBorderWarningReachPacket::new),
                    entry(CameraPacket.class, CameraPacket::new),
                    entry(HeldItemChangePacket.class, HeldItemChangePacket::new),
                    entry(UpdateViewPositionPacket.class, UpdateViewPositionPacket::new),
                    entry(UpdateViewDistancePacket.class, UpdateViewDistancePacket::new),
                    entry(SpawnPositionPacket.class, SpawnPositionPacket::new),
                    entry(DisplayScoreboardPacket.class, DisplayScoreboardPacket::new),
                    entry(EntityMetaDataPacket.class, EntityMetaDataPacket::new),
                    entry(AttachEntityPacket.class, AttachEntityPacket::new),
                    entry(EntityVelocityPacket.class, EntityVelocityPacket::new),
                    entry(EntityEquipmentPacket.class, EntityEquipmentPacket::new),
                    entry(SetExperiencePacket.class, SetExperiencePacket::new),
                    entry(UpdateHealthPacket.class, UpdateHealthPacket::new),
                    entry(ScoreboardObjectivePacket.class, ScoreboardObjectivePacket::new),
                    entry(SetPassengersPacket.class, SetPassengersPacket::new),
                    entry(TeamsPacket.class, TeamsPacket::new),
                    entry(UpdateScorePacket.class, UpdateScorePacket::new),
                    entry(UpdateSimulationDistancePacket.class, UpdateSimulationDistancePacket::new),
                    entry(SetTitleSubTitlePacket.class, SetTitleSubTitlePacket::new),
                    entry(TimeUpdatePacket.class, TimeUpdatePacket::new),
                    entry(SetTitleTextPacket.class, SetTitleTextPacket::new),
                    entry(SetTitleTimePacket.class, SetTitleTimePacket::new),
                    entry(EntitySoundEffectPacket.class, EntitySoundEffectPacket::new),
                    entry(SoundEffectPacket.class, SoundEffectPacket::new),
                    entry(StartConfigurationPacket.class, StartConfigurationPacket::new),
                    entry(StopSoundPacket.class, StopSoundPacket::new),
                    entry(CookieStorePacket.class, CookieStorePacket::new),
                    entry(SystemChatPacket.class, SystemChatPacket::new),
                    entry(PlayerListHeaderAndFooterPacket.class, PlayerListHeaderAndFooterPacket::new),
                    entry(NbtQueryResponsePacket.class, NbtQueryResponsePacket::new),
                    entry(CollectItemPacket.class, CollectItemPacket::new),
                    entry(EntityTeleportPacket.class, EntityTeleportPacket::new),
                    entry(SetTickStatePacket.class, SetTickStatePacket::new),
                    entry(TickStepPacket.class, TickStepPacket::new),
                    entry(TransferPacket.class, TransferPacket::new),
                    entry(AdvancementsPacket.class, AdvancementsPacket::new),
                    entry(EntityAttributesPacket.class, EntityAttributesPacket::new),
                    entry(EntityEffectPacket.class, EntityEffectPacket::new),
                    entry(DeclareRecipesPacket.class, DeclareRecipesPacket::new),
                    entry(TagsPacket.class, TagsPacket::new),
                    entry(ProjectilePowerPacket.class, ProjectilePowerPacket::new),
                    entry(CustomReportDetailsPacket.class, CustomReportDetailsPacket::new),
                    entry(ServerLinksPacket.class, ServerLinksPacket::new)
            );
        }
    }

    sealed class PacketRegistryTemplate<T> implements PacketRegistry<T> {
        private final Entry<T>[] suppliers;
        private final ClassValue<Integer> packetIds = new ClassValue<>() {
            @Override
            protected Integer computeValue(@NotNull Class<?> type) {
                for (int i = 0; i < suppliers.length; i++) {
                    if (suppliers[i].type == type) return i;
                }
                throw new IllegalStateException("Packet type " + type + " isn't registered!");
            }
        };

        @SafeVarargs
        PacketRegistryTemplate(Entry<T>... suppliers) {
            this.suppliers = suppliers;
        }

        public @UnknownNullability T create(int packetId, @NotNull NetworkBuffer reader) {
            final Entry<T> entry = suppliers[packetId];
            final NetworkBuffer.Reader<T> supplier = entry.reader;
            if (supplier == null)
                throw new IllegalStateException("Packet id 0x" + Integer.toHexString(packetId) + " isn't registered!");
            return supplier.read(reader);
        }

        @Override
        public int packetId(@NotNull Class<? extends T> packetClass) {
            return packetIds.get(packetClass);
        }

        record Entry<T>(Class<T> type, NetworkBuffer.Reader<T> reader) {
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        static <T> Entry<T> entry(Class<? extends T> type, NetworkBuffer.Reader<? extends T> reader) {
            return new Entry<>((Class) type, reader);
        }
    }
}
