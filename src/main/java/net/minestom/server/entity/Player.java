package net.minestom.server.entity;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.bossbar.BossBar;
import net.minestom.server.chat.Chat;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.command.CommandManager;
import net.minestom.server.effects.Effects;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.vehicle.PlayerVehicleInformation;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.item.PickupExperienceEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.login.JoinGamePacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.resourcepack.ResourcePack;
import net.minestom.server.scoreboard.BelowNameScoreboard;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.Sound;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.stat.PlayerStatistic;
import net.minestom.server.timer.TaskRunnable;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Dimension;
import net.minestom.server.world.LevelType;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Player extends LivingEntity {

	private long lastKeepAlive;
	private boolean answerKeepAlive;

	private String username;
	protected PlayerConnection playerConnection;
	private ConcurrentLinkedQueue<ClientPlayPacket> packets = new ConcurrentLinkedQueue<>();

	private int latency;
	private String displayName;
	private PlayerSkin skin;

	private Dimension dimension;
	private GameMode gameMode;
	private LevelType levelType;
	private int teleportId = 0;

	protected boolean onGround;

	protected Set<Entity> viewableEntities = new CopyOnWriteArraySet<>();
	protected Set<Chunk> viewableChunks = new CopyOnWriteArraySet<>();

	private PlayerSettings settings;
	private float exp;
	private int level;
	private PlayerInventory inventory;
	private short heldSlot;
	private Inventory openInventory;

	private Position respawnPoint;

	private float additionalHearts;
	private int food;
	private float foodSaturation;
	private long startEatingTime;
	private long defaultEatingTime = 1000L;
	private long eatingTime;
	private boolean isEating;

	// Game state (https://wiki.vg/Protocol#Change_Game_State)
	private boolean enableRespawnScreen;

	// CustomBlock break delay
	private CustomBlock targetCustomBlock;
	private BlockPosition targetBlockPosition;
	private long targetBlockTime;
	private byte targetLastStage;
	private int blockBreakTime;

	private Set<BossBar> bossBars = new CopyOnWriteArraySet<>();
	private Team team;
	private BelowNameScoreboard belowNameScoreboard;

	/**
	 * Last damage source to hit this player, used to display the death message.
	 */
	private DamageType lastDamageSource;

	private int permissionLevel;

	private boolean reducedDebugScreenInformation;

	// Abilities
	private boolean invulnerable;
	private boolean flying;
	private boolean allowFlying;
	private boolean instantBreak;
	private float flyingSpeed = 0.05f;
	private float fieldViewModifier = 0.1f;

	// Statistics
	private Map<PlayerStatistic, Integer> statisticValueMap = new Hashtable<>();

	// Vehicle
	private PlayerVehicleInformation vehicleInformation = new PlayerVehicleInformation();

	// Tick related
	private final PlayerTickEvent playerTickEvent = new PlayerTickEvent(this);

	public Player(UUID uuid, String username, PlayerConnection playerConnection) {
		super(EntityType.PLAYER);
		this.uuid = uuid; // Override Entity#uuid defined in the constructor
		this.username = username;
		this.playerConnection = playerConnection;

		setBoundingBox(0.69f, 1.8f, 0.69f);

		setRespawnPoint(new Position(0, 0, 0));

		this.settings = new PlayerSettings();
		this.inventory = new PlayerInventory(this);

		setCanPickupItem(true); // By default

		// Allow the server to send the next keep alive packet
		refreshAnswerKeepAlive(true);

		this.gameMode = GameMode.SURVIVAL;
		this.dimension = Dimension.OVERWORLD;
		this.levelType = LevelType.FLAT;
		refreshPosition(0, 0, 0);

		// FakePlayer init its connection there
		playerConnectionInit();

		MinecraftServer.getEntityManager().addWaitingPlayer(this);
	}

	/**
	 * Used when the player is created
	 * Init the player and spawn him
	 */
	protected void init() {

		// Init player (register events)
		for (Consumer<Player> playerInitialization : MinecraftServer.getConnectionManager().getPlayerInitializations()) {
			playerInitialization.accept(this);
		}

		// TODO complete login sequence with optionals packets
		JoinGamePacket joinGamePacket = new JoinGamePacket();
		joinGamePacket.entityId = getEntityId();
		joinGamePacket.gameMode = gameMode;
		joinGamePacket.dimension = dimension;
		joinGamePacket.maxPlayers = 0; // Unused
		joinGamePacket.levelType = levelType;
		joinGamePacket.viewDistance = MinecraftServer.CHUNK_VIEW_DISTANCE;
		joinGamePacket.reducedDebugInfo = false;
		playerConnection.sendPacket(joinGamePacket);

		// TODO minecraft:brand plugin message

		ServerDifficultyPacket serverDifficultyPacket = new ServerDifficultyPacket();
		serverDifficultyPacket.difficulty = MinecraftServer.getDifficulty();
		serverDifficultyPacket.locked = true;
		playerConnection.sendPacket(serverDifficultyPacket);

		SpawnPositionPacket spawnPositionPacket = new SpawnPositionPacket();
		spawnPositionPacket.x = 0;
		spawnPositionPacket.y = 0;
		spawnPositionPacket.z = 0;
		playerConnection.sendPacket(spawnPositionPacket);

		// Add player to list with spawning skin
		PlayerSkinInitEvent skinInitEvent = new PlayerSkinInitEvent(this);
		callEvent(PlayerSkinInitEvent.class, skinInitEvent);
		this.skin = skinInitEvent.getSkin();
		playerConnection.sendPacket(getAddPlayerToList());

		// Commands start
		{
			CommandManager commandManager = MinecraftServer.getCommandManager();
			DeclareCommandsPacket declareCommandsPacket = commandManager.createDeclareCommandsPacket(this);

			playerConnection.sendPacket(declareCommandsPacket);
		}
		// Commands end


		// Recipes start
		{
			RecipeManager recipeManager = MinecraftServer.getRecipeManager();
			DeclareRecipesPacket declareRecipesPacket = recipeManager.getDeclareRecipesPacket();
			if (declareRecipesPacket.recipes != null) {
				playerConnection.sendPacket(declareRecipesPacket);
			}

			List<String> recipesIdentifier = new ArrayList<>();
			for (Recipe recipe : recipeManager.getRecipes()) {
				if (!recipe.shouldShow(this))
					continue;

				recipesIdentifier.add(recipe.getRecipeId());
			}
			if (!recipesIdentifier.isEmpty()) {
				String[] identifiers = recipesIdentifier.toArray(new String[0]);
				UnlockRecipesPacket unlockRecipesPacket = new UnlockRecipesPacket();
				unlockRecipesPacket.mode = 0;
				unlockRecipesPacket.recipesId = identifiers;
				unlockRecipesPacket.initRecipesId = identifiers;
				playerConnection.sendPacket(unlockRecipesPacket);
			}
		}
		// Recipes end

		// Some client update
		playerConnection.sendPacket(getPropertiesPacket()); // Send default properties
		refreshHealth(); // Heal and send health packet
		refreshAbilities(); // Send abilities packet
		getInventory().update();
	}

	/**
	 * Used to initialize the player connection
	 */
	protected void playerConnectionInit() {
		this.playerConnection.setPlayer(this);
	}

	@Override
	public boolean damage(DamageType type, float value) {
		if (isInvulnerable())
			return false;

		// Compute final heart based on health and additional hearts
		boolean result = super.damage(type, value);
		if (result) {
			lastDamageSource = type;
		}
		return result;
	}

	@Override
	public void update(long time) {
		// Flush all pending packets
		playerConnection.flush();

		// Process received packets
		ClientPlayPacket packet;
		while ((packet = packets.poll()) != null) {
			packet.process(this);
		}

		super.update(time); // Super update (item pickup/fire management)

		// Target block stage
		if (targetCustomBlock != null) {
			final byte animationCount = 10;
			long since = time - targetBlockTime;
			byte stage = (byte) (since / (blockBreakTime / animationCount));
			stage = MathUtils.setBetween(stage, (byte) -1, animationCount);
			if (stage != targetLastStage) {
				sendBlockBreakAnimation(targetBlockPosition, stage);
			}
			this.targetLastStage = stage;
			if (stage > 9) {
				instance.breakBlock(this, targetBlockPosition);
				resetTargetBlock();
			}
		}

		// Experience orb pickup
		Chunk chunk = instance.getChunkAt(getPosition()); // TODO check surrounding chunks
		Set<Entity> entities = instance.getChunkEntities(chunk);
		for (Entity entity : entities) {
			if (entity instanceof ExperienceOrb) {
				ExperienceOrb experienceOrb = (ExperienceOrb) entity;
				BoundingBox itemBoundingBox = experienceOrb.getBoundingBox();
				if (expandedBoundingBox.intersect(itemBoundingBox)) {
					synchronized (experienceOrb) {
						if (experienceOrb.shouldRemove() || experienceOrb.isRemoveScheduled())
							continue;
						PickupExperienceEvent pickupExperienceEvent = new PickupExperienceEvent(experienceOrb);
						callCancellableEvent(PickupExperienceEvent.class, pickupExperienceEvent, () -> {
							short experienceCount = pickupExperienceEvent.getExperienceCount(); // TODO give to player
							entity.remove();
						});
					}
				}
			}
		}

		// Eating animation
		if (isEating()) {
			if (time - startEatingTime >= eatingTime) {
				refreshEating(false);

				triggerStatus((byte) 9); // Mark item use as finished
				ItemUpdateStateEvent itemUpdateStateEvent = callItemUpdateStateEvent(true);

				// Refresh hand
				boolean isOffHand = itemUpdateStateEvent.getHand() == Player.Hand.OFF;
				refreshActiveHand(false, isOffHand, false);

				ItemStack foodItem = itemUpdateStateEvent.getItemStack();
				boolean isFood = foodItem.getMaterial().isFood();

				if (isFood) {
					PlayerEatEvent playerEatEvent = new PlayerEatEvent(this, foodItem);
					callEvent(PlayerEatEvent.class, playerEatEvent);
				}
			}
		}

		// Tick event
		callEvent(PlayerTickEvent.class, playerTickEvent);

		// Multiplayer sync
		final boolean positionChanged = position.getX() != lastX || position.getY() != lastY || position.getZ() != lastZ;
		final boolean viewChanged = position.getYaw() != lastYaw || position.getPitch() != lastPitch;
		if (!getViewers().isEmpty() && (positionChanged || viewChanged)) {
			ServerPacket updatePacket = null;
			ServerPacket optionalUpdatePacket = null;
			if (positionChanged && viewChanged) {
				EntityPositionAndRotationPacket entityPositionAndRotationPacket = new EntityPositionAndRotationPacket();
				entityPositionAndRotationPacket.entityId = getEntityId();
				entityPositionAndRotationPacket.deltaX = (short) ((position.getX() * 32 - lastX * 32) * 128);
				entityPositionAndRotationPacket.deltaY = (short) ((position.getY() * 32 - lastY * 32) * 128);
				entityPositionAndRotationPacket.deltaZ = (short) ((position.getZ() * 32 - lastZ * 32) * 128);
				entityPositionAndRotationPacket.yaw = position.getYaw();
				entityPositionAndRotationPacket.pitch = position.getPitch();
				entityPositionAndRotationPacket.onGround = onGround;

				lastX = position.getX();
				lastY = position.getY();
				lastZ = position.getZ();
				lastYaw = position.getYaw();
				lastPitch = position.getPitch();
				updatePacket = entityPositionAndRotationPacket;
			} else if (positionChanged) {
				EntityPositionPacket entityPositionPacket = new EntityPositionPacket();
				entityPositionPacket.entityId = getEntityId();
				entityPositionPacket.deltaX = (short) ((position.getX() * 32 - lastX * 32) * 128);
				entityPositionPacket.deltaY = (short) ((position.getY() * 32 - lastY * 32) * 128);
				entityPositionPacket.deltaZ = (short) ((position.getZ() * 32 - lastZ * 32) * 128);
				entityPositionPacket.onGround = onGround;
				lastX = position.getX();
				lastY = position.getY();
				lastZ = position.getZ();
				updatePacket = entityPositionPacket;
			} else if (viewChanged) {
				EntityRotationPacket entityRotationPacket = new EntityRotationPacket();
				entityRotationPacket.entityId = getEntityId();
				entityRotationPacket.yaw = position.getYaw();
				entityRotationPacket.pitch = position.getPitch();
				entityRotationPacket.onGround = onGround;

				lastYaw = position.getYaw();
				lastPitch = position.getPitch();
				updatePacket = entityRotationPacket;
			}

			if (viewChanged) {
				EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
				entityHeadLookPacket.entityId = getEntityId();
				entityHeadLookPacket.yaw = position.getYaw();
				optionalUpdatePacket = entityHeadLookPacket;
			}

			if (updatePacket != null) {
				if (optionalUpdatePacket != null) {
					sendPacketsToViewers(updatePacket, optionalUpdatePacket);
				} else {
					sendPacketToViewers(updatePacket);
				}
			}
		}

	}

	@Override
	public void kill() {
		if (!isDead()) {
			// send death message to player
			Component deathMessage;
			if (lastDamageSource != null) {
				deathMessage = lastDamageSource.buildDeathScreenMessage(this);
			} else { // may happen if killed by the server without applying damage
				deathMessage = TextComponent.of("Killed by poor programming.");
			}
			CombatEventPacket deathPacket = CombatEventPacket.death(this, Optional.empty(), deathMessage);
			playerConnection.sendPacket(deathPacket);

			// send death message to chat
			Component chatMessage;
			if (lastDamageSource != null) {
				chatMessage = lastDamageSource.buildChatMessage(this);
			} else { // may happen if killed by the server without applying damage
				chatMessage = TextComponent.of(getUsername() + " was killed by poor programming.");
			}
			MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {
				player.sendMessage(chatMessage);
			});
		}
		super.kill();
	}

	/**
	 * Respawn the player by sending a {@link RespawnPacket} to the player and teleporting him
	 * to {@link #getRespawnPoint()}. It also reset fire and his health
	 */
	public void respawn() {
		if (!isDead())
			return;

		setFireForDuration(0);
		setOnFire(false);
		refreshHealth();
		RespawnPacket respawnPacket = new RespawnPacket();
		respawnPacket.dimension = getDimension();
		respawnPacket.gameMode = getGameMode();
		respawnPacket.levelType = getLevelType();
		getPlayerConnection().sendPacket(respawnPacket);
		PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(this, getRespawnPoint());
		callEvent(PlayerRespawnEvent.class, respawnEvent);
		refreshIsDead(false);

		// Runnable called when teleportation is successful (after loading and sending necessary chunk)
		teleport(respawnEvent.getRespawnPosition(), this::refreshAfterTeleport);
	}

	@Override
	public void spawn() {

	}

	@Override
	public boolean isOnGround() {
		return onGround;
	}

	@Override
	public void remove() {
		super.remove();
		this.packets.clear();
		clearBossBars();
		if (getOpenInventory() != null)
			getOpenInventory().removeViewer(this);
		this.viewableEntities.forEach(entity -> entity.removeViewer(this));
		this.viewableChunks.forEach(chunk -> {
			if (chunk.isLoaded())
				chunk.removeViewer(this);
		});
		resetTargetBlock();
		callEvent(PlayerDisconnectEvent.class, new PlayerDisconnectEvent(this));
		playerConnection.disconnect();
	}

	@Override
	public boolean addViewer(Player player) {
		if (player == this)
			return false;

		boolean result = super.addViewer(player);
		if (!result)
			return false;

		PlayerConnection viewerConnection = player.getPlayerConnection();
		showPlayer(viewerConnection);
		return result;
	}

	@Override
	public boolean removeViewer(Player player) {
		if (player == this)
			return false;

		boolean result = super.removeViewer(player);
		PlayerConnection viewerConnection = player.getPlayerConnection();
		viewerConnection.sendPacket(getRemovePlayerToList());

		// Team
		if (team != null && team.getPlayers().size() == 1) // If team only contains "this" player
			viewerConnection.sendPacket(team.createTeamDestructionPacket());
		return result;
	}

	@Override
	public void setInstance(Instance instance) {
		Check.notNull(instance, "instance cannot be null!");
		Check.argCondition(this.instance == instance, "Instance should be different than the current one");

		boolean firstSpawn = this.instance == null; // TODO: Handle player reconnections, must be false in that case too
		for (Chunk viewableChunk : viewableChunks) {
			viewableChunk.removeViewer(this);
		}
		viewableChunks.clear();

		if (this.instance != null) {
			Dimension instanceDimension = instance.getDimension();
			if (dimension != instanceDimension)
				sendDimension(instanceDimension);
		}

		long[] visibleChunks = ChunkUtils.getChunksInRange(position, getChunkRange());
		int length = visibleChunks.length;

		AtomicInteger counter = new AtomicInteger(0);
		for (int i = 0; i < length; i++) {
			int[] chunkPos = ChunkUtils.getChunkCoord(visibleChunks[i]);
			int chunkX = chunkPos[0];
			int chunkZ = chunkPos[1];
			Consumer<Chunk> callback = (chunk) -> {
				if (chunk != null) {
					viewableChunks.add(chunk);
					chunk.addViewer(this);
					instance.sendChunk(this, chunk);
					updateViewPosition(chunk);
				}
				boolean isLast = counter.get() == length - 1;
				if (isLast) {
					// This is the last chunk to be loaded , spawn player
					super.setInstance(instance);
					PlayerSpawnEvent spawnEvent = new PlayerSpawnEvent(instance, firstSpawn);
					callEvent(PlayerSpawnEvent.class, spawnEvent);
				} else {
					// Increment the counter of current loaded chunks
					counter.incrementAndGet();
				}
			};

			// WARNING: if auto load is disabled and no chunks are loaded beforehand, player will be stuck.
			instance.loadOptionalChunk(chunkX, chunkZ, callback);
		}
	}

	@Override
	public Consumer<PacketWriter> getMetadataConsumer() {
		return packet -> {
			super.getMetadataConsumer().accept(packet);
			fillMetadataIndex(packet, 14);
			fillMetadataIndex(packet, 16);
		};
	}

	@Override
	protected void fillMetadataIndex(PacketWriter packet, int index) {
		super.fillMetadataIndex(packet, index);
		if (index == 14) {
			packet.writeByte((byte) 14);
			packet.writeByte(METADATA_FLOAT);
			packet.writeFloat(additionalHearts);
		} else if (index == 16) {
			packet.writeByte((byte) 16);
			packet.writeByte(METADATA_BYTE);
			packet.writeByte(getSettings().getDisplayedSkinParts());
		}
	}

	/**
	 * Send a {@link BlockBreakAnimationPacket} packet to the player and his viewers
	 * Setting {@code destroyStage} to -1 reset the break animation
	 *
	 * @param blockPosition the position of the block
	 * @param destroyStage  the destroy stage
	 * @throws IllegalArgumentException if {@code destroyStage} is not between -1 and 10
	 */
	public void sendBlockBreakAnimation(BlockPosition blockPosition, byte destroyStage) {
		Check.argCondition(!MathUtils.isBetween(destroyStage, -1, 10),
				"The destroy stage has to be between -1 and 10");
		BlockBreakAnimationPacket breakAnimationPacket = new BlockBreakAnimationPacket();
		breakAnimationPacket.entityId = getEntityId() + 1;
		breakAnimationPacket.blockPosition = blockPosition;
		breakAnimationPacket.destroyStage = destroyStage;
		sendPacketToViewersAndSelf(breakAnimationPacket);
	}

	// Use legacy color formatting
	public void sendMessage(String message) {
		sendMessage(Chat.fromLegacyText(message));
	}

	/**
	 * Send a message to the player
	 *
	 * @param message   the message to send
	 * @param colorChar the character used to represent the color
	 */
	public void sendMessage(String message, char colorChar) {
		sendMessage(Chat.fromLegacyText(message, colorChar));
	}

	/**
	 * Send a message to the player
	 *
	 * @param component the text component
	 */
	public void sendMessage(Component component) {
		String json = Chat.toJsonString(component);
		playerConnection.sendPacket(new ChatMessagePacket(json, ChatMessagePacket.Position.CHAT));
	}

	public void playSound(Sound sound, SoundCategory soundCategory, int x, int y, int z, float volume, float pitch) {
		SoundEffectPacket soundEffectPacket = new SoundEffectPacket();
		soundEffectPacket.soundId = sound.getId();
		soundEffectPacket.soundCategory = soundCategory;
		soundEffectPacket.x = x * 8;
		soundEffectPacket.y = y * 8;
		soundEffectPacket.z = z * 8;
		soundEffectPacket.volume = volume;
		soundEffectPacket.pitch = pitch;
		playerConnection.sendPacket(soundEffectPacket);
	}

	/**
	 * Plays a given effect at the given position for this player
	 *
	 * @param effect                the effect to play
	 * @param x                     x position of the effect
	 * @param y                     y position of the effect
	 * @param z                     z position of the effect
	 * @param data                  data for the effect
	 * @param disableRelativeVolume disable volume scaling based on distance
	 */
	public void playEffect(Effects effect, int x, int y, int z, int data, boolean disableRelativeVolume) {
		EffectPacket packet = new EffectPacket();
		packet.effectId = effect.getId();
		packet.position = new BlockPosition(x, y, z);
		packet.data = data;
		packet.disableRelativeVolume = disableRelativeVolume;
		playerConnection.sendPacket(packet);
	}

	/**
	 * Send a {@link StopSoundPacket} packet
	 */
	public void stopSound() {
		StopSoundPacket stopSoundPacket = new StopSoundPacket();
		stopSoundPacket.flags = 0x00;
		playerConnection.sendPacket(stopSoundPacket);
	}

	public void sendHeaderFooter(Component header, Component footer) {
		PlayerListHeaderAndFooterPacket playerListHeaderAndFooterPacket = new PlayerListHeaderAndFooterPacket();
		playerListHeaderAndFooterPacket.emptyHeader = header == null;
		playerListHeaderAndFooterPacket.emptyFooter = footer == null;
		playerListHeaderAndFooterPacket.header = Chat.toJsonString(header);
		playerListHeaderAndFooterPacket.footer = Chat.toJsonString(footer);

		playerConnection.sendPacket(playerListHeaderAndFooterPacket);
	}

	public void sendHeaderFooter(String header, String footer, char colorChar) {
		sendHeaderFooter(Chat.fromLegacyText(header, colorChar), Chat.fromLegacyText(footer, colorChar));
	}

	private void sendTitle(Component title, TitlePacket.Action action) {
		TitlePacket titlePacket = new TitlePacket();
		titlePacket.action = action;

		switch (action) {
			case SET_TITLE:
				titlePacket.titleText = Chat.toJsonString(title);
				break;
			case SET_SUBTITLE:
				titlePacket.subtitleText = Chat.toJsonString(title);
				break;
			case SET_ACTION_BAR:
				titlePacket.actionBarText = Chat.toJsonString(title);
			default:
				throw new UnsupportedOperationException("Invalid TitlePacket.Action type!");
		}

		playerConnection.sendPacket(titlePacket);
	}

	public void sendTitleSubtitleMessage(Component title, Component subtitle) {
		sendTitle(title, TitlePacket.Action.SET_TITLE);
		sendTitle(subtitle, TitlePacket.Action.SET_SUBTITLE);
	}

	public void sendTitleMessage(Component title) {
		sendTitle(title, TitlePacket.Action.SET_TITLE);
	}

	public void sendTitleMessage(String title, char colorChar) {
		sendTitleMessage(Chat.fromLegacyText(title, colorChar));
	}

	public void sendTitleMessage(String title) {
		sendTitleMessage(title, Chat.COLOR_CHAR);
	}

	public void sendSubtitleMessage(Component subtitle) {
		sendTitle(subtitle, TitlePacket.Action.SET_SUBTITLE);
	}

	public void sendSubtitleMessage(String subtitle, char colorChar) {
		sendSubtitleMessage(Chat.fromLegacyText(subtitle, colorChar));
	}

	public void sendSubtitleMessage(String subtitle) {
		sendSubtitleMessage(subtitle, Chat.COLOR_CHAR);
	}

	public void sendActionBarMessage(Component actionBar) {
		sendTitle(actionBar, TitlePacket.Action.SET_ACTION_BAR);
	}

	public void sendActionBarMessage(String message, char colorChar) {
		sendActionBarMessage(Chat.fromLegacyText(message, colorChar));
	}

	public void sendActionBarMessage(String message) {
		sendActionBarMessage(message, Chat.COLOR_CHAR);
	}

	@Override
	public boolean isImmune(DamageType type) {
		if (!getGameMode().canTakeDamage()) {
			return type != DamageType.VOID;
		}
		return super.isImmune(type);
	}

	@Override
	public void setAttribute(Attribute attribute, float value) {
		super.setAttribute(attribute, value);
		if (playerConnection != null)
			playerConnection.sendPacket(getPropertiesPacket());
	}

	@Override
	public void setHealth(float health) {
		super.setHealth(health);
		sendUpdateHealthPacket();
	}

	/**
	 * Get the player additional hearts
	 *
	 * @return the player additional hearts
	 */
	public float getAdditionalHearts() {
		return additionalHearts;
	}

	/**
	 * Update the internal field and send the appropriate {@link EntityMetaDataPacket}
	 *
	 * @param additionalHearts the count of additional heartss
	 */
	public void setAdditionalHearts(float additionalHearts) {
		this.additionalHearts = additionalHearts;
		sendMetadataIndex(14);
	}

	/**
	 * Get the player food
	 *
	 * @return the player food
	 */
	public int getFood() {
		return food;
	}

	/**
	 * Set and refresh client food bar
	 *
	 * @param food the new food value
	 */
	public void setFood(int food) {
		Check.argCondition(!MathUtils.isBetween(food, 0, 20), "Food has to be between 0 and 20");
		this.food = food;
		sendUpdateHealthPacket();
	}

	public float getFoodSaturation() {
		return foodSaturation;
	}

	/**
	 * Set and refresh client food saturation
	 *
	 * @param foodSaturation the food saturation
	 */
	public void setFoodSaturation(float foodSaturation) {
		Check.argCondition(!MathUtils.isBetween(foodSaturation, 0, 5), "Food saturation has to be between 0 and 5");
		this.foodSaturation = foodSaturation;
		sendUpdateHealthPacket();
	}

	/**
	 * Get if the player is eating
	 *
	 * @return true if the player is eating, false otherwise
	 */
	public boolean isEating() {
		return isEating;
	}

	/**
	 * Get the player default eating time
	 *
	 * @return the player default eating time
	 */
	public long getDefaultEatingTime() {
		return defaultEatingTime;
	}

	/**
	 * Used to change the default eating time animation
	 *
	 * @param defaultEatingTime the default eating time in milliseconds
	 */
	public void setDefaultEatingTime(long defaultEatingTime) {
		this.defaultEatingTime = defaultEatingTime;
	}

	/**
	 * Get the player display name in the tab-list
	 *
	 * @return the player display name,
	 * null means that {@link #getUsername()} is displayed
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Change the player display name in the tab-list
	 * <p>
	 * Set to null to show the player username
	 *
	 * @param displayName the display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;

		String jsonDisplayName = displayName != null ? Chat.toJsonString(Chat.fromLegacyText(displayName)) : null;
		PlayerInfoPacket infoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_DISPLAY_NAME);
		infoPacket.playerInfos.add(new PlayerInfoPacket.UpdateDisplayName(getUuid(), jsonDisplayName));
		sendPacketToViewersAndSelf(infoPacket);
	}

	/**
	 * Get the player skin
	 *
	 * @return the player skin object,
	 * null means that the player has his {@link #getUuid()} default skin
	 */
	public PlayerSkin getSkin() {
		return skin;
	}

	/**
	 * Change the player skin
	 *
	 * @param skin the player skin, null to reset it to his {@link #getUuid()} default skin
	 */
	public synchronized void setSkin(PlayerSkin skin) {
		this.skin = skin;

		DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket();
		destroyEntitiesPacket.entityIds = new int[]{getEntityId()};

		PlayerInfoPacket removePlayerPacket = getRemovePlayerToList();
		PlayerInfoPacket addPlayerPacket = getAddPlayerToList();

		RespawnPacket respawnPacket = new RespawnPacket();
		respawnPacket.dimension = getDimension();
		respawnPacket.gameMode = getGameMode();
		respawnPacket.levelType = getLevelType();

		playerConnection.sendPacket(removePlayerPacket);
		playerConnection.sendPacket(destroyEntitiesPacket);
		playerConnection.sendPacket(respawnPacket);
		playerConnection.sendPacket(addPlayerPacket);

		for (Player viewer : getViewers()) {
			PlayerConnection connection = viewer.getPlayerConnection();

			connection.sendPacket(removePlayerPacket);
			connection.sendPacket(destroyEntitiesPacket);

			showPlayer(connection);
		}

		getInventory().update();
		teleport(getPosition());
	}

	/**
	 * Used to update the internal skin field
	 *
	 * @param skin the player skin
	 * @see #setSkin(PlayerSkin) instead
	 */
	protected void refreshSkin(PlayerSkin skin) {
		this.skin = skin;
	}

	/**
	 * Get if the player has the respawn screen enabled or disabled
	 *
	 * @return true if the player has the respawn screen, false if he didn't
	 */
	public boolean isEnableRespawnScreen() {
		return enableRespawnScreen;
	}

	/**
	 * Enable or disable the respawn screen
	 *
	 * @param enableRespawnScreen true to enable the respawn screen, false to disable it
	 */
	public void setEnableRespawnScreen(boolean enableRespawnScreen) {
		this.enableRespawnScreen = enableRespawnScreen;
		sendChangeGameStatePacket(ChangeGameStatePacket.Reason.ENABLE_RESPAWN_SCREEN, enableRespawnScreen ? 0 : 1);
	}

	/**
	 * Get the player username
	 *
	 * @return the player username
	 */
	public String getUsername() {
		return username;
	}

	private void sendChangeGameStatePacket(ChangeGameStatePacket.Reason reason, float value) {
		ChangeGameStatePacket changeGameStatePacket = new ChangeGameStatePacket();
		changeGameStatePacket.reason = reason;
		changeGameStatePacket.value = value;
		playerConnection.sendPacket(changeGameStatePacket);
	}

	/**
	 * @param item the item to drop
	 * @return true if player can drop the item (event not cancelled), false otherwise
	 */
	public boolean dropItem(ItemStack item) {
		ItemDropEvent itemDropEvent = new ItemDropEvent(item);
		callEvent(ItemDropEvent.class, itemDropEvent);
		return !itemDropEvent.isCancelled();
	}

	/**
	 * Set the player resource pack
	 *
	 * @param resourcePack the resource pack
	 */
	public void setResourcePack(ResourcePack resourcePack) {
		Check.notNull(resourcePack, "The resource pack cannot be null");
		final String url = resourcePack.getUrl();
		Check.notNull(url, "The resource pack url cannot be null");
		final String hash = resourcePack.getHash();

		ResourcePackSendPacket resourcePackSendPacket = new ResourcePackSendPacket();
		resourcePackSendPacket.url = url;
		resourcePackSendPacket.hash = hash;
		playerConnection.sendPacket(resourcePackSendPacket);
	}

	/**
	 * Rotate the player to face {@code targetPosition}
	 *
	 * @param facePoint      the point from where the player should aim
	 * @param targetPosition the target position to face
	 */
	public void facePosition(FacePoint facePoint, Position targetPosition) {
		facePosition(facePoint, targetPosition, null, null);
	}

	/**
	 * Rotate the player to face {@code entity}
	 *
	 * @param facePoint   the point from where the player should aim
	 * @param entity      the entity to face
	 * @param targetPoint the point to aim at {@code entity} position
	 */
	public void facePosition(FacePoint facePoint, Entity entity, FacePoint targetPoint) {
		facePosition(facePoint, entity.getPosition(), entity, targetPoint);
	}

	private void facePosition(FacePoint facePoint, Position targetPosition, Entity entity, FacePoint targetPoint) {
		FacePlayerPacket facePlayerPacket = new FacePlayerPacket();
		facePlayerPacket.entityFacePosition = facePoint == FacePoint.EYE ?
				FacePlayerPacket.FacePosition.EYES : FacePlayerPacket.FacePosition.FEET;
		facePlayerPacket.targetX = targetPosition.getX();
		facePlayerPacket.targetY = targetPosition.getY();
		facePlayerPacket.targetZ = targetPosition.getZ();
		if (entity != null) {
			facePlayerPacket.entityId = entity.getEntityId();
			facePlayerPacket.entityFacePosition = targetPoint == FacePoint.EYE ?
					FacePlayerPacket.FacePosition.EYES : FacePlayerPacket.FacePosition.FEET;
		}
		playerConnection.sendPacket(facePlayerPacket);
	}

	/**
	 * Set the camera at {@code entity} eyes
	 *
	 * @param entity the entity to spectate
	 */
	public void spectate(Entity entity) {
		CameraPacket cameraPacket = new CameraPacket();
		cameraPacket.cameraId = entity.getEntityId();
		playerConnection.sendPacket(cameraPacket);
	}

	/**
	 * Reset the camera at the player
	 */
	public void stopSpectating() {
		spectate(this);
	}

	/**
	 * Used to retrieve the default spawn point
	 * can be altered by the {@link PlayerRespawnEvent#setRespawnPosition(Position)}
	 *
	 * @return the default respawn point
	 */
	public Position getRespawnPoint() {
		return respawnPoint;
	}

	/**
	 * Change the default spawn point
	 *
	 * @param respawnPoint the player respawn point
	 */
	public void setRespawnPoint(Position respawnPoint) {
		this.respawnPoint = respawnPoint;
	}

	/**
	 * Called after the player teleportation to refresh his position
	 * and send data to his new viewers
	 */
	protected void refreshAfterTeleport() {
		getInventory().update();

		SpawnPlayerPacket spawnPlayerPacket = new SpawnPlayerPacket();
		spawnPlayerPacket.entityId = getEntityId();
		spawnPlayerPacket.playerUuid = getUuid();
		spawnPlayerPacket.position = getPosition();
		sendPacketToViewers(spawnPlayerPacket);

		// Update for viewers
		sendPacketToViewersAndSelf(getVelocityPacket());
		sendPacketToViewersAndSelf(getMetadataPacket());
		playerConnection.sendPacket(getPropertiesPacket());
		syncEquipments();

		sendSynchronization();
	}

	protected void refreshHealth() {
		this.food = 20;
		this.foodSaturation = 5;
		// refresh health and send health packet
		heal();
	}

	protected void sendUpdateHealthPacket() {
		UpdateHealthPacket updateHealthPacket = new UpdateHealthPacket();
		updateHealthPacket.health = getHealth();
		updateHealthPacket.food = food;
		updateHealthPacket.foodSaturation = foodSaturation;
		playerConnection.sendPacket(updateHealthPacket);
	}

	/**
	 * Used to change the percentage experience bar
	 * This cannot change the displayed level, see {@link #setLevel(int)}
	 *
	 * @param exp a percentage between 0 and 1
	 */
	public void setExp(float exp) {
		Check.argCondition(!MathUtils.isBetween(exp, 0, 1), "Exp should be between 0 and 1");

		this.exp = exp;
		sendExperienceUpdatePacket();
	}

	/**
	 * Used to change the level of the player
	 * This cannot change the displayed percentage bar see {@link #setExp(float)}
	 *
	 * @param level the new level of the player
	 */
	public void setLevel(int level) {
		this.level = level;
		sendExperienceUpdatePacket();
	}

	protected void sendExperienceUpdatePacket() {
		SetExperiencePacket setExperiencePacket = new SetExperiencePacket();
		setExperiencePacket.percentage = exp;
		setExperiencePacket.level = level;
		playerConnection.sendPacket(setExperiencePacket);
	}

	protected void onChunkChange(Chunk lastChunk, Chunk newChunk) {
		long[] lastVisibleChunks = ChunkUtils.getChunksInRange(new Position(16 * lastChunk.getChunkX(), 0, 16 * lastChunk.getChunkZ()), MinecraftServer.CHUNK_VIEW_DISTANCE);
		long[] updatedVisibleChunks = ChunkUtils.getChunksInRange(new Position(16 * newChunk.getChunkX(), 0, 16 * newChunk.getChunkZ()), MinecraftServer.CHUNK_VIEW_DISTANCE);
		int[] oldChunks = ArrayUtils.getDifferencesBetweenArray(lastVisibleChunks, updatedVisibleChunks);
		int[] newChunks = ArrayUtils.getDifferencesBetweenArray(updatedVisibleChunks, lastVisibleChunks);

		// Unload old chunks
		for (int index : oldChunks) {
			int[] chunkPos = ChunkUtils.getChunkCoord(lastVisibleChunks[index]);
			UnloadChunkPacket unloadChunkPacket = new UnloadChunkPacket();
			unloadChunkPacket.chunkX = chunkPos[0];
			unloadChunkPacket.chunkZ = chunkPos[1];
			playerConnection.sendPacket(unloadChunkPacket);

			Chunk chunk = instance.getChunk(chunkPos[0], chunkPos[1]);
			if (chunk != null)
				chunk.removeViewer(this);
		}

		updateViewPosition(newChunk);

		// Load new chunks
		for (int i = 0; i < newChunks.length; i++) {
			int index = newChunks[i];
			int[] chunkPos = ChunkUtils.getChunkCoord(updatedVisibleChunks[index]);
			instance.loadOptionalChunk(chunkPos[0], chunkPos[1], chunk -> {
				if (chunk == null) {
					// Cannot load chunk (auto load is not enabled)
					return;
				}
				this.viewableChunks.add(chunk);
				chunk.addViewer(this);
				instance.sendChunk(this, chunk);
			});
		}
	}

	@Override
	public void teleport(Position position, Runnable callback) {
		super.teleport(position, () -> {
			updatePlayerPosition();
			if (callback != null)
				callback.run();
		});
	}

	@Override
	public void teleport(Position position) {
		teleport(position, null);
	}

	/**
	 * Get the player connection
	 * <p>
	 * Used to send packets and get relatives stuff to the connection
	 *
	 * @return the player connection
	 */
	public PlayerConnection getPlayerConnection() {
		return playerConnection;
	}

	/**
	 * Get if the player is online or not
	 *
	 * @return true if the player is online, false otherwise
	 */
	public boolean isOnline() {
		return playerConnection.isOnline();
	}

	/**
	 * Get the player settings
	 *
	 * @return the player settings
	 */
	public PlayerSettings getSettings() {
		return settings;
	}

	/**
	 * Get the player dimension
	 *
	 * @return the player current dimension
	 */
	public Dimension getDimension() {
		return dimension;
	}

	public PlayerInventory getInventory() {
		return inventory;
	}

	/**
	 * Used to get the player latency,
	 * computed by seeing how long it takes the client to answer the {@link KeepAlivePacket} packet
	 *
	 * @return the player latency
	 */
	public int getLatency() {
		return latency;
	}

	/**
	 * Get the player GameMode
	 *
	 * @return the player current gamemode
	 */
	public GameMode getGameMode() {
		return gameMode;
	}

	/**
	 * Change the player GameMode
	 *
	 * @param gameMode the new player GameMode
	 */
	public void setGameMode(GameMode gameMode) {
		Check.notNull(gameMode, "GameMode cannot be null");
		this.gameMode = gameMode;
		sendChangeGameStatePacket(ChangeGameStatePacket.Reason.CHANGE_GAMEMODE, gameMode.getId());

		PlayerInfoPacket infoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_GAMEMODE);
		infoPacket.playerInfos.add(new PlayerInfoPacket.UpdateGamemode(getUuid(), gameMode));
		sendPacketToViewersAndSelf(infoPacket);
	}

	/**
	 * Returns true iff this player is in creative. Used for code readability
	 *
	 * @return true if the player is in creative mode, false otherwise
	 */
	public boolean isCreative() {
		return gameMode == GameMode.CREATIVE;
	}

	/**
	 * Change the dimension of the player
	 * Mostly unsafe since it requires sending chunks after
	 *
	 * @param dimension the new player dimension
	 */
	public void sendDimension(Dimension dimension) {
		Check.notNull(dimension, "Dimension cannot be null!");
		Check.argCondition(dimension.equals(getDimension()), "The dimension need to be different than the current one!");

		this.dimension = dimension;
		RespawnPacket respawnPacket = new RespawnPacket();
		respawnPacket.dimension = dimension;
		respawnPacket.gameMode = gameMode;
		respawnPacket.levelType = levelType;
		playerConnection.sendPacket(respawnPacket);
	}

	/**
	 * Kick the player with a reason
	 *
	 * @param textComponent the kick reason
	 */
	public void kick(TextComponent textComponent) {
		DisconnectPacket disconnectPacket = new DisconnectPacket();
		disconnectPacket.message = Chat.toJsonString(textComponent);
		playerConnection.sendPacket(disconnectPacket);
		playerConnection.disconnect();
	}

	/**
	 * Kick the player with a reason
	 *
	 * @param message the kick reason
	 */
	public void kick(String message) {
		kick(Chat.fromLegacyText(message));
	}

	public LevelType getLevelType() {
		return levelType;
	}

	/**
	 * Change the current held slot for the player
	 *
	 * @param slot the slot that the player has to held
	 * @throws IllegalArgumentException if {@code slot} is not between 0 and 8
	 */
	public void setHeldItemSlot(short slot) {
		Check.argCondition(!MathUtils.isBetween(slot, 0, 8), "Slot has to be between 0 and 8");

		HeldItemChangePacket heldItemChangePacket = new HeldItemChangePacket();
		heldItemChangePacket.slot = slot;
		playerConnection.sendPacket(heldItemChangePacket);
		refreshHeldSlot(slot);
	}

	/**
	 * Get the player held slot (0-8)
	 *
	 * @return the current held slot for the player
	 */
	public short getHeldSlot() {
		return heldSlot;
	}

	public void setTeam(Team team) {
		if (this.team == team)
			return;

		if (this.team != null) {
			this.team.removePlayer(this);
		}

		this.team = team;
		if (team != null) {
			team.addPlayer(this);
			sendPacketToViewers(team.getTeamsCreationPacket()); // FIXME: only if viewer hasn't already register this team
		}
	}

	public void setBelowNameScoreboard(BelowNameScoreboard belowNameScoreboard) {
		if (this.belowNameScoreboard == belowNameScoreboard)
			return;

		if (this.belowNameScoreboard != null) {
			this.belowNameScoreboard.removeViewer(this);
		}

		this.belowNameScoreboard = belowNameScoreboard;
		if (belowNameScoreboard != null) {
			belowNameScoreboard.addViewer(this);
			belowNameScoreboard.displayScoreboard(this);
			getViewers().forEach(player -> belowNameScoreboard.addViewer(player));
		}
	}

	/**
	 * Get the player open inventory
	 *
	 * @return the currently open inventory, null if there is not (player inventory is not detected)
	 */
	public Inventory getOpenInventory() {
		return openInventory;
	}

	/**
	 * Used to get the {@link CustomBlock} that the player is currently mining
	 *
	 * @return the currently mined {@link CustomBlock} by the player, null if there is not
	 */
	public CustomBlock getCustomBlockTarget() {
		return targetCustomBlock;
	}

	/**
	 * @return an unmodifiable {@link Set} containing all the current player viewable boss bars
	 */
	public Set<BossBar> getBossBars() {
		return Collections.unmodifiableSet(bossBars);
	}

	/**
	 * Open the specified Inventory, close the previous inventory if existing
	 *
	 * @param inventory the inventory to open
	 * @return true if the inventory has been opened/sent to the player, false otherwise (cancelled by event)
	 */
	public boolean openInventory(Inventory inventory) {
		Check.notNull(inventory, "Inventory cannot be null, use Player#closeInventory() to close current");

		InventoryOpenEvent inventoryOpenEvent = new InventoryOpenEvent(this, inventory);

		callCancellableEvent(InventoryOpenEvent.class, inventoryOpenEvent, () -> {

			if (getOpenInventory() != null) {
				closeInventory();
			}

			Inventory newInventory = inventoryOpenEvent.getInventory();

			OpenWindowPacket openWindowPacket = new OpenWindowPacket();
			openWindowPacket.windowId = newInventory.getWindowId();
			openWindowPacket.windowType = newInventory.getInventoryType().getWindowType();
			openWindowPacket.title = newInventory.getTitle();
			playerConnection.sendPacket(openWindowPacket);
			newInventory.addViewer(this);
			this.openInventory = newInventory;

		});

		return !inventoryOpenEvent.isCancelled();
	}

	/**
	 * Close the current inventory if there is any
	 * It closes the player inventory if {@link #getOpenInventory()} returns null
	 */
	public void closeInventory() {
		Inventory openInventory = getOpenInventory();

		// Drop cursor item when closing inventory
		ItemStack cursorItem;
		if (openInventory == null) {
			cursorItem = getInventory().getCursorItem();
			getInventory().setCursorItem(ItemStack.getAirItem());
		} else {
			cursorItem = openInventory.getCursorItem(this);
		}
		if (!cursorItem.isAir()) {
			// Add item to inventory if he hasn't been able to drop it
			if (!dropItem(cursorItem)) {
				getInventory().addItemStack(cursorItem);
			}
		}

		CloseWindowPacket closeWindowPacket = new CloseWindowPacket();
		if (openInventory == null) {
			closeWindowPacket.windowId = 0;
		} else {
			closeWindowPacket.windowId = openInventory.getWindowId();
			openInventory.removeViewer(this); // Clear cache
			this.openInventory = null;
		}
		playerConnection.sendPacket(closeWindowPacket);
		inventory.update();
	}

	/**
	 * Get the player viewable chunks
	 *
	 * @return an unmodifiable {@link Set} containing all the chunks that the player sees
	 */
	public Set<Chunk> getViewableChunks() {
		return Collections.unmodifiableSet(viewableChunks);
	}

	/**
	 * Remove all the boss bars that the player has
	 */
	public void clearBossBars() {
		this.bossBars.forEach(bossBar -> bossBar.removeViewer(this));
	}

	/**
	 * Send a {@link UpdateViewPositionPacket}  to the player
	 *
	 * @param chunk the chunk to update the view
	 */
	public void updateViewPosition(Chunk chunk) {
		UpdateViewPositionPacket updateViewPositionPacket = new UpdateViewPositionPacket();
		updateViewPositionPacket.chunkX = chunk.getChunkX();
		updateViewPositionPacket.chunkZ = chunk.getChunkZ();
		playerConnection.sendPacket(updateViewPositionPacket);
	}

	/**
	 * Used for synchronization purpose, mainly for teleportation
	 */
	protected void updatePlayerPosition() {
		PlayerPositionAndLookPacket positionAndLookPacket = new PlayerPositionAndLookPacket();
		positionAndLookPacket.position = position;
		positionAndLookPacket.flags = 0x00;
		positionAndLookPacket.teleportId = teleportId++;
		playerConnection.sendPacket(positionAndLookPacket);
	}

	/**
	 * Get the player permission level
	 *
	 * @return the player permission level
	 */
	public int getPermissionLevel() {
		return permissionLevel;
	}

	/**
	 * Change the player permission level
	 *
	 * @param permissionLevel the new player permission level
	 */
	public void setPermissionLevel(int permissionLevel) {
		Check.argCondition(!MathUtils.isBetween(permissionLevel, 0, 4), "permissionLevel has to be between 0 and 4");

		this.permissionLevel = permissionLevel;

		// Magic values: https://wiki.vg/Entity_statuses#Player
		byte permissionLevelStatus = (byte) (24 + permissionLevel);
		triggerStatus(permissionLevelStatus);
	}

	/**
	 * Set or remove the reduced debug screen
	 *
	 * @param reduced should the player has the reduced debug screen
	 */
	public void setReducedDebugScreenInformation(boolean reduced) {
		this.reducedDebugScreenInformation = reduced;

		// Magic values: https://wiki.vg/Entity_statuses#Player
		byte debugScreenStatus = (byte) (reduced ? 22 : 23);
		triggerStatus(debugScreenStatus);
	}

	/**
	 * Get if the player has the reduced debug screen
	 *
	 * @return true if the player has the reduced debug screen, false otherwise
	 */
	public boolean hasReducedDebugScreenInformation() {
		return reducedDebugScreenInformation;
	}

	/**
	 * The invulnerable field appear in the {@link PlayerAbilitiesPacket} packet
	 *
	 * @return true if the player is invulnerable, false otherwise
	 */
	public boolean isInvulnerable() {
		return invulnerable;
	}

	/**
	 * This do update the {@code invulnerable} field in the packet {@link PlayerAbilitiesPacket}
	 * and prevent the player from receiving damage
	 *
	 * @param invulnerable should the player be invulnerable
	 */
	public void setInvulnerable(boolean invulnerable) {
		this.invulnerable = invulnerable;
		refreshAbilities();
	}

	/**
	 * Get if the player is currently flying
	 *
	 * @return true if the player if flying, false otherwise
	 */
	public boolean isFlying() {
		return flying;
	}

	/**
	 * Set the player flying
	 *
	 * @param flying should the player fly
	 */
	public void setFlying(boolean flying) {
		this.flying = flying;
		refreshAbilities();
	}

	/**
	 * Update the internal flying field
	 * <p>
	 * Mostly unsafe since there is nothing to backup the value, used internally for creative players
	 *
	 * @param flying the new flying field
	 * @see #setFlying(boolean) instead
	 */
	public void refreshFlying(boolean flying) {
		this.flying = flying;
	}

	/**
	 * Get if the player is allowed to fly
	 *
	 * @return true if the player if allowed to fly, false otherwise
	 */
	public boolean isAllowFlying() {
		return allowFlying;
	}

	/**
	 * Allow or forbid the player to fly
	 *
	 * @param allowFlying should the player be allowed to fly
	 */
	public void setAllowFlying(boolean allowFlying) {
		this.allowFlying = allowFlying;
		refreshAbilities();
	}

	public boolean isInstantBreak() {
		return instantBreak;
	}

	/**
	 * Change the player ability "Creative Mode"
	 * <a href="https://wiki.vg/Protocol#Player_Abilities_.28clientbound.29">see</a>
	 * <p>
	 * WARNING: this has nothing to do with {@link CustomBlock#getBreakDelay(Player, BlockPosition)}
	 *
	 * @param instantBreak
	 */
	public void setInstantBreak(boolean instantBreak) {
		this.instantBreak = instantBreak;
		refreshAbilities();
	}

	/**
	 * Get the player flying speed
	 *
	 * @return the flying speed of the player
	 */
	public float getFlyingSpeed() {
		return flyingSpeed;
	}

	/**
	 * Update the internal field and send a {@link PlayerAbilitiesPacket} with the new flying speed
	 *
	 * @param flyingSpeed the new flying speed of the player
	 */
	public void setFlyingSpeed(float flyingSpeed) {
		this.flyingSpeed = flyingSpeed;
		refreshAbilities();
	}

	public float getFieldViewModifier() {
		return fieldViewModifier;
	}

	public void setFieldViewModifier(float fieldViewModifier) {
		this.fieldViewModifier = fieldViewModifier;
		refreshAbilities();
	}

	/**
	 * This is the map used to send the statistic packet
	 * It is possible to add/remove/change statistic value directly into it
	 *
	 * @return the modifiable statistic map
	 */
	public Map<PlayerStatistic, Integer> getStatisticValueMap() {
		return statisticValueMap;
	}

	/**
	 * Get the player vehicle information
	 *
	 * @return the player vehicle information
	 */
	public PlayerVehicleInformation getVehicleInformation() {
		return vehicleInformation;
	}

	protected void refreshAbilities() {
		PlayerAbilitiesPacket playerAbilitiesPacket = new PlayerAbilitiesPacket();
		playerAbilitiesPacket.invulnerable = invulnerable;
		playerAbilitiesPacket.flying = flying;
		playerAbilitiesPacket.allowFlying = allowFlying;
		playerAbilitiesPacket.instantBreak = instantBreak;
		playerAbilitiesPacket.flyingSpeed = flyingSpeed;
		playerAbilitiesPacket.fieldViewModifier = fieldViewModifier;

		playerConnection.sendPacket(playerAbilitiesPacket);
	}

	/**
	 * All packets in the queue are executed in the {@link #update(long)} method
	 * It is used internally to add all received packet from the client
	 * Could be used to "simulate" a received packet, but to use at your own risk
	 *
	 * @param packet the packet to add in the queue
	 */
	public void addPacketToQueue(ClientPlayPacket packet) {
		this.packets.add(packet);
	}

	/**
	 * Change the storage player latency and update its tab value
	 *
	 * @param latency the new player latency
	 */
	public void refreshLatency(int latency) {
		this.latency = latency;
		PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_LATENCY);
		playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateLatency(getUuid(), latency));
		sendPacketToViewersAndSelf(playerInfoPacket);
	}

	public void refreshOnGround(boolean onGround) {
		this.onGround = onGround;
	}

	/**
	 * Used to change internally the last sent last keep alive id
	 * <p>
	 * Warning: could lead to have the player kicked because of a wrong keep alive packet
	 *
	 * @param lastKeepAlive the new lastKeepAlive id
	 */
	public void refreshKeepAlive(long lastKeepAlive) {
		this.lastKeepAlive = lastKeepAlive;
		this.answerKeepAlive = false;
	}

	public boolean didAnswerKeepAlive() {
		return answerKeepAlive;
	}

	public void refreshAnswerKeepAlive(boolean answerKeepAlive) {
		this.answerKeepAlive = answerKeepAlive;
	}

	/**
	 * Change the held item for the player viewers
	 * Also cancel eating if {@link #isEating()} was true
	 * <p>
	 * Warning: the player will not be noticed by this chance, only his viewers,
	 * see instead: {@link #setHeldItemSlot(short)}
	 *
	 * @param slot the new held slot
	 */
	public void refreshHeldSlot(short slot) {
		this.heldSlot = slot;
		syncEquipment(EntityEquipmentPacket.Slot.MAIN_HAND);

		refreshEating(false);
	}

	public void refreshEating(boolean isEating, long eatingTime) {
		this.isEating = isEating;
		if (isEating) {
			this.startEatingTime = System.currentTimeMillis();
			this.eatingTime = eatingTime;
		} else {
			this.startEatingTime = 0;
		}
	}

	public void refreshEating(boolean isEating) {
		refreshEating(isEating, defaultEatingTime);
	}

	/**
	 * Used to call {@link ItemUpdateStateEvent} with the proper item
	 * It does check which hand to get the item to update
	 *
	 * @param allowFood true if food should be updated, false otherwise
	 * @return the called {@link ItemUpdateStateEvent},
	 * null if there is no item to update the state
	 */
	public ItemUpdateStateEvent callItemUpdateStateEvent(boolean allowFood) {
		Material mainHandMat = Material.fromId(getItemInMainHand().getMaterialId());
		Material offHandMat = Material.fromId(getItemInOffHand().getMaterialId());
		boolean isOffhand = offHandMat.hasState();

		ItemStack updatedItem = isOffhand ? getItemInOffHand() :
				mainHandMat.hasState() ? getItemInMainHand() : null;
		if (updatedItem == null) // No item with state, cancel
			return null;

		boolean isFood = updatedItem.getMaterial().isFood();

		if (isFood && !allowFood)
			return null;

		ItemUpdateStateEvent itemUpdateStateEvent = new ItemUpdateStateEvent(updatedItem,
				isOffhand ? Hand.OFF : Hand.MAIN);
		callEvent(ItemUpdateStateEvent.class, itemUpdateStateEvent);

		return itemUpdateStateEvent;
	}

	/**
	 * Make the player digging a custom block, see {@link #resetTargetBlock()} to rewind
	 *
	 * @param targetCustomBlock   the custom block to dig
	 * @param targetBlockPosition the custom block position
	 * @param breakTime           the time it will take to break the block in milliseconds
	 */
	public void setTargetBlock(CustomBlock targetCustomBlock, BlockPosition targetBlockPosition, int breakTime) {
		this.targetCustomBlock = targetCustomBlock;
		this.targetBlockPosition = targetBlockPosition;
		this.targetBlockTime = targetBlockPosition == null ? 0 : System.currentTimeMillis();
		this.blockBreakTime = breakTime;
	}

	/**
	 * Reset data from the current block the player is mining.
	 * If the currently mined block (or if there isn't any) is not a CustomBlock, nothing append
	 */
	public void resetTargetBlock() {
		if (targetBlockPosition != null) {
			sendBlockBreakAnimation(targetBlockPosition, (byte) -1); // Clear the break animation
			this.targetCustomBlock = null;
			this.targetBlockPosition = null;
			this.targetBlockTime = 0;

			// Remove effect
			RemoveEntityEffectPacket removeEntityEffectPacket = new RemoveEntityEffectPacket();
			removeEntityEffectPacket.entityId = getEntityId();
			removeEntityEffectPacket.effectId = 4;
			getPlayerConnection().sendPacket(removeEntityEffectPacket);
		}
	}

	/**
	 * Used internally to add Bossbar in the {@link #getBossBars()} set
	 * You probably want to use {@link BossBar#addViewer(Player)}
	 *
	 * @param bossBar the bossbar to add internally
	 */
	public void refreshAddBossbar(BossBar bossBar) {
		this.bossBars.add(bossBar);
	}

	/**
	 * Used internally to remove Bossbar from the {@link #getBossBars()} set
	 * You probably want to use {@link BossBar#removeViewer(Player)}
	 *
	 * @param bossBar the bossbar to remove internally
	 */
	public void refreshRemoveBossbar(BossBar bossBar) {
		this.bossBars.remove(bossBar);
	}

	public void refreshVehicleSteer(float sideways, float forward, boolean jump, boolean unmount) {
		this.vehicleInformation.refresh(sideways, forward, jump, unmount);
	}

	/**
	 * @return the chunk range of the viewers,
	 * which is {@link MinecraftServer#CHUNK_VIEW_DISTANCE} or {@link PlayerSettings#getViewDistance()}
	 * based on which one is the lowest
	 */
	public int getChunkRange() {
		int serverRange = MinecraftServer.CHUNK_VIEW_DISTANCE;
		int playerRange = getSettings().viewDistance;
		if (playerRange == 0) {
			return serverRange; // Didn't receive settings packet yet (is the case on login)
		} else {
			return Math.min(playerRange, serverRange);
		}
	}

	/**
	 * Get the last sent keep alive id
	 *
	 * @return the last keep alive id sent to the player
	 */
	public long getLastKeepAlive() {
		return lastKeepAlive;
	}

	/**
	 * Get the packet to add the player from tab-list
	 *
	 * @return a {@link PlayerInfoPacket} to add the player
	 */
	protected PlayerInfoPacket getAddPlayerToList() {
		final String textures = skin == null ? "" : skin.getTextures();
		final String signature = skin == null ? null : skin.getSignature();

		String jsonDisplayName = displayName != null ? Chat.toJsonString(Chat.fromLegacyText(displayName)) : null;
		PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);

		PlayerInfoPacket.AddPlayer addPlayer =
				new PlayerInfoPacket.AddPlayer(getUuid(), getUsername(), getGameMode(), getLatency());
		addPlayer.displayName = jsonDisplayName;

		PlayerInfoPacket.AddPlayer.Property prop =
				new PlayerInfoPacket.AddPlayer.Property("textures", textures, signature);
		addPlayer.properties.add(prop);

		playerInfoPacket.playerInfos.add(addPlayer);
		return playerInfoPacket;
	}

	/**
	 * Get the packet to remove the player from tab-list
	 *
	 * @return a {@link PlayerInfoPacket} to add the player
	 */
	protected PlayerInfoPacket getRemovePlayerToList() {
		PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER);

		PlayerInfoPacket.RemovePlayer removePlayer =
				new PlayerInfoPacket.RemovePlayer(getUuid());

		playerInfoPacket.playerInfos.add(removePlayer);
		return playerInfoPacket;
	}

	/**
	 * Send all the related packet to have the player sent to another with related data
	 * (create player, spawn position, velocity, metadata, equipments, passengers, team)
	 * <p>
	 * WARNING: this does not sync the player, please use {@link #addViewer(Player)}
	 *
	 * @param connection the connection to show the player to
	 */
	protected void showPlayer(PlayerConnection connection) {
		SpawnPlayerPacket spawnPlayerPacket = new SpawnPlayerPacket();
		spawnPlayerPacket.entityId = getEntityId();
		spawnPlayerPacket.playerUuid = getUuid();
		spawnPlayerPacket.position = getPosition();
		connection.sendPacket(getAddPlayerToList());

		connection.sendPacket(spawnPlayerPacket);
		connection.sendPacket(getVelocityPacket());
		connection.sendPacket(getMetadataPacket());

		// Equipments synchronization
		syncEquipments(connection);

		if (hasPassenger()) {
			connection.sendPacket(getPassengersPacket());
		}

		// Team
		if (team != null)
			connection.sendPacket(team.getTeamsCreationPacket());

		EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
		entityHeadLookPacket.entityId = getEntityId();
		entityHeadLookPacket.yaw = position.getYaw();
		connection.sendPacket(entityHeadLookPacket);
	}

	@Override
	public ItemStack getItemInMainHand() {
		return inventory.getItemInMainHand();
	}

	@Override
	public void setItemInMainHand(ItemStack itemStack) {
		inventory.setItemInMainHand(itemStack);
	}

	@Override
	public ItemStack getItemInOffHand() {
		return inventory.getItemInOffHand();
	}

	@Override
	public void setItemInOffHand(ItemStack itemStack) {
		inventory.setItemInOffHand(itemStack);
	}

	@Override
	public ItemStack getHelmet() {
		return inventory.getHelmet();
	}

	@Override
	public void setHelmet(ItemStack itemStack) {
		inventory.setHelmet(itemStack);
	}

	@Override
	public ItemStack getChestplate() {
		return inventory.getChestplate();
	}

	@Override
	public void setChestplate(ItemStack itemStack) {
		inventory.setChestplate(itemStack);
	}

	@Override
	public ItemStack getLeggings() {
		return inventory.getLeggings();
	}

	@Override
	public void setLeggings(ItemStack itemStack) {
		inventory.setLeggings(itemStack);
	}

	@Override
	public ItemStack getBoots() {
		return inventory.getBoots();
	}

	@Override
	public void setBoots(ItemStack itemStack) {
		inventory.setBoots(itemStack);
	}

	/**
	 * Represent the main or off hand of the player
	 */
	public enum Hand {
		MAIN,
		OFF
	}

	public enum FacePoint {
		FEET,
		EYE
	}

	// Settings enum

	/**
	 * Represent where is located the main hand of the player (can be changed in Minecraft option)
	 */
	public enum MainHand {
		LEFT,
		RIGHT
	}

	public enum ChatMode {
		ENABLED,
		COMMANDS_ONLY,
		HIDDEN
	}

	public class PlayerSettings {

		private String locale;
		private byte viewDistance;
		private ChatMode chatMode;
		private boolean chatColors;
		private byte displayedSkinParts;
		private MainHand mainHand;

		/**
		 * The player game language
		 *
		 * @return the player locale
		 */
		public String getLocale() {
			return locale;
		}

		/**
		 * Get the player view distance
		 *
		 * @return the player view distance
		 */
		public byte getViewDistance() {
			return viewDistance;
		}

		/**
		 * Get the player chat mode
		 *
		 * @return the player chat mode
		 */
		public ChatMode getChatMode() {
			return chatMode;
		}

		/**
		 * Get if the player has chat colors enabled
		 *
		 * @return true if chat colors are enabled, false otherwise
		 */
		public boolean hasChatColors() {
			return chatColors;
		}

		public byte getDisplayedSkinParts() {
			return displayedSkinParts;
		}

		/**
		 * Get the player main hand
		 *
		 * @return the player main hand
		 */
		public MainHand getMainHand() {
			return mainHand;
		}

		/**
		 * Change the player settings internally
		 * <p>
		 * WARNING: the player will not be noticed by this change, probably unsafe
		 *
		 * @param locale             the player locale
		 * @param viewDistance       the player view distance
		 * @param chatMode           the player chat mode
		 * @param chatColors         the player chat colors
		 * @param displayedSkinParts the player displayed skin parts
		 * @param mainHand           the player main handœ
		 */
		public void refresh(String locale, byte viewDistance, ChatMode chatMode, boolean chatColors, byte displayedSkinParts, MainHand mainHand) {
			this.locale = locale;
			this.viewDistance = viewDistance;
			this.chatMode = chatMode;
			this.chatColors = chatColors;
			this.displayedSkinParts = displayedSkinParts;
			this.mainHand = mainHand;
			sendMetadataIndex(16);
		}

	}

}
