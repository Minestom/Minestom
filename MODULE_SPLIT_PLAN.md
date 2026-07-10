# Splitting Minestom into `:lib` and `:framework`

A plan to break the monolithic engine into two Gradle modules: an unopinionated
`:lib` (protocol, data, codecs — everything `Scratch.java` needs) and an
opinionated `:framework` (`ServerProcess`, events, ticking, live objects).

- **Source of truth:** analysis of all ~1400 files under `net.minestom.server.*`.
- **Acceptance test:** `demo/.../Scratch.java` must compile against `:lib` alone.
- **Breaking changes:** 3 high, ~6 medium, ~10 low. Everything else is non-breaking by construction.

---

## 1. Goal and principles

**Goal.** Split root project `:` (module `net.minestom.server`) into:

- **`:lib`** — unopinionated structs and API. Pure protocol, data, codecs:
  `NetworkBuffer` + every packet, the `Registries` interface + registry/codec/tag
  machinery, coordinates, block/material/item/world **data**, enums and flags.
  Everything a plain-socket server needs to *speak the protocol and hold data*
  with no running server. No lifecycle, no ticking, no events, no service locator.
- **`:framework`** — opinionated control flow. `ServerProcess`, the tick loop,
  the event system, listeners, live `Instance`/`Chunk`/`Entity`/`Player`/`Inventory`
  objects, command dispatch, scheduler, threads/`Acquirable`, monitoring, snapshots.
  **`:framework` depends on `:lib` (`api`).**

**The one hard rule.** `framework → lib` is allowed. `lib → framework` is a
violation. Every lib-destined type that references a framework anchor
(`MinecraftServer` statics, `ServerProcess`, `ServerFlag` today, `event.*`,
`listener.*`, `thread.*`, `timer.*`, `monitoring.*`, `snapshot.*`,
`exception.ExceptionManager`, and the live `Instance`/`Chunk`/`Entity`/`Player`/
`Inventory`/command-dispatch runtime) must be severed before it can move to `:lib`.

**Litmus test applied to every type.** *"Could `Scratch` (plain socket + virtual
threads, no `ServerProcess`) use this to speak the protocol / hold data?"* Yes →
`:lib`. *"Does it only make sense with a running tick/event/lifecycle?"* → `:framework`.

**Design stances that fall out of the rule:**

1. **Config constants are data, not lifecycle.** `ServerFlag` is a pure leaf (only
   `System.getProperty` + JetBrains annotations). It moves *wholesale* to `:lib`.
   `framework → lib` reads of tick/keep-alive constants are legal; this single move
   dissolves ~15 config crossings with zero code churn.
2. **Service-locator reach-through is replaced by lib-level holders/hooks, not by
   moving code up.** A lib default-`Registries` holder, a registry freeze-state hook,
   a tag-invalidation hook, and local loggers replace `MinecraftServer.process()/
   getExceptionManager()/LOGGER` inside lib serialization code — without changing
   public signatures.
3. **Convenience overloads that touch live `Entity`/`Player`/`Instance` are amputated
   from otherwise-pure data types** and re-homed on the framework side (e.g.
   `Potion.sendAddPacket(Entity)`, `WorldBorder.inBounds(Entity)`,
   `RelativeVec.from(Entity)`). The record + `CODEC` + `NETWORK_TYPE` stay in lib.
4. **Packages split internally where data and runtime cohabit** (e.g. `instance`:
   `Section`/palette/light/generator/heightmap-encode = lib, `Instance`/`Chunk`/
   loaders = framework). The split line is drawn at the file/member level.

---

## 2. Module layout — package by package

Legend: **L** = `:lib`, **F** = `:framework`, **SPLIT** = internal split (exact
line given). Every currently-exported package is listed.

### 2.1 Network / packets

| Package | Module | Split line / note |
|---|---|---|
| `network` | **SPLIT** | L: `NetworkBuffer(+Impl)`, `NetworkBufferTypeImpl`, `NetworkBufferTemplate(+Impl)`, `ComponentNetworkBufferTypeImpl`, `ConnectionState`. F: `ConnectionManager`, `PlayerProvider` |
| `network.packet` | **L** | `Packet`, `PacketParser`, `PacketRegistry` (+`PacketInfo`, `ConnectionSide`), `PacketReading`, `PacketWriting`, `PacketVanilla` |
| `network.packet.client` (+ `common`, `configuration`, `handshake`, `login`, `play`, `status`) | **L** | `ClientPacket` sealed base + all client records |
| `network.packet.server` | **L** | `ServerPacket`, `SendablePacket`, `FramedPacket`, `BufferedPacket`, `CachedPacket` |
| `network.packet.server.common` / `.configuration` / `.login` / `.play` (+`play.data`) / `.status` | **L** | ~150 play packets + config/login/status/common; all pure records/serializers |
| `network.socket` | **F** | `Server` (NIO accept/select loop) |
| `network.player` | **SPLIT** | L: `GameProfile`, `ClientSettings`, `ResolvableProfile`. F: `PlayerConnection`, `PlayerSocketConnection` |
| `network.plugin` | **F** | `LoginPlugin`, `LoginPluginMessageProcessor` |
| `network.debug` (+`info`) | **L** | `DebugSubscription`, `Event`, `Update`, `Debug*Info` payloads |

### 2.2 Registry / codec / tag / component / data

| Package | Module | Note |
|---|---|---|
| `registry` | **L** | `Registries(+Delegating)`, `VanillaRegistries`, `DynamicRegistry(+Impl)`, `Registry`, `StaticRegistry`, `RegistryKey`, `RegistryTag`, `TagKey`, `Holder`, `HolderSet`, `StaticProtocolObject`, `RegistryData`, `RegistryTranscoder`, `RegistryCodecs`, `RegistryNetworkTypes` |
| `codec` | **L** | `Codec`, `StructCodec`, `Transcoder` (+Nbt/Json/Java/Crc32/Proxy), `Result`, `Encoder`, `Decoder`, `ComponentCodecs` |
| `tag` | **L** | `Tag`, `TagHandler(+Impl)`, `Taggable`, `TagSerializer`, `Serializers`, `TagNbtSeparator`, `StaticIntMap` |
| `component` | **L** | `DataComponent(+Impl)`, `DataComponentMap(+Impl)`, `DataComponents` |
| `gamedata` | **L** | `DataPack(+Impl)` |
| `condition` | **L** | `DataPredicate(+Noop)` — pure codec/NBT |

### 2.3 Entity

| Package | Module | Split line / note |
|---|---|---|
| `entity` (top level) | **SPLIT** | L: `EntityType(+Impl)`, `EntityPose`, `GameMode`, `EquipmentSlot`, `EquipmentSlotGroup`, `MainHand`, `PlayerHand`, `RelativeFlags`, `EntityStatuses`, `EntityActivity`, `VillagerProfession(+Impl)`, `PlayerSkin`, `Metadata(+Impl)`, `MetadataDef(+Impl)`, `MetadataHolder`. F: `Entity`, `LivingEntity`, `Player`, `EntityCreature`, `EntityProjectile`, `ItemEntity`, `ExperienceOrb`, `EntityView` |
| `entity.metadata` (+ all subpackages) | **L** | Entire ~130-class meta tree + variant registry data + `ObjectDataProvider`, after `EntityMeta` is decoupled from `Entity` via lib `MetaTarget` |
| `entity.attribute` | **L** | `Attribute(+Impl)`, `AttributeInstance`, `AttributeModifier`, `AttributeOperation`; `PROTECTED_MODIFIERS` relocated into lib |
| `entity.damage` | **SPLIT** | L: `DamageType(+Impl)`. F: `Damage`, `EntityDamage`, `EntityProjectileDamage`, `PositionalDamage` |
| `entity.ai` (+`goal`, `target`) | **F** | Live creature behavior |
| `entity.pathfinding` (+`followers`, `generators`) | **F** | Live navigation |
| `entity.vehicle` | **L** | `PlayerInputs` |

### 2.4 Instance / world storage

| Package | Module | Split line / note |
|---|---|---|
| `instance` | **SPLIT** | L: `Section`, `Weather`, `WorldBorder` (after 2 crossings). F: `Instance`, `InstanceContainer`, `SharedInstance`, `Chunk`, `DynamicChunk`, `LightingChunk`, `EntityTracker(+Impl)`, `InstanceManager`, `ChunkLoader`, `NoopChunkLoaderImpl`, `Clock`, `Explosion`, `ExplosionSupplier` |
| `instance.block` | **SPLIT** | L: `Block`, `BlockImpl`, `Blocks`, `BlockFace`, `BlockEntityType(+Impl)`, `BlockSoundType(+Impl)`, and the lib identity super-interface of `BlockHandler` (`getKey`, `getBlockEntityTags`, `getBlockEntityAction`, `isTickable`). F: `BlockManager` + `BlockHandler`'s callback layer (`onPlace/onDestroy/onInteract/onTouch/tick` + `Placement`/`Destroy`/`Interaction`/`Touch`/`Tick`) |
| `instance.block.banner` / `.jukebox` | **L** | `BannerPattern(+Impl)`, `JukeboxSong(+Impl)` |
| `instance.block.predicate` | **L** | `BlockPredicate`, `PropertiesPredicate`, `DataComponentPredicates` |
| `instance.block.rule` | **L** | `BlockPlacementRule` — decoupled via `Block.Getter` |
| `instance.generator` | **L** | `Generator`, `GenerationUnit`, `UnitModifier`, `GeneratorImpl(+GenSection)` |
| `instance.heightmap` | **SPLIT** | L: `Heightmap.Type` enum + static `Heightmap.encode(short[],int)` (extracted to a lib type). F: the stateful `Heightmap(Chunk)` base + `MotionBlockingHeightmap`, `WorldSurfaceHeightmap` |
| `instance.palette` | **L** | `Palette(+Impl)`, `Palettes` |
| `instance.light` | **L** | `Light`, `SkyLight`, `BlockLight`, `LightCompute`; `getNeighbors(Chunk,int)` relocates to framework `LightingChunk` |
| `instance.batch` | **F** | `Batch`, `AbsoluteBlockBatch`, `ChunkBatch`, `RelativeBlockBatch`, `BatchOption` |
| `instance.anvil` | **F** | `AnvilLoader`, `RegionFile` |
| `instance.fluid` / `.gamerule` | **L** | `Fluid(+Impl)`, `GameRule(+Impl)` (identity data; per-instance values live on framework `Instance`) |

### 2.5 Item / inventory

| Package | Module | Split line / note |
|---|---|---|
| `item` | **L** | `ItemStack(+Impl,+HashImpl,+Template)`, `Material(+Impl)`, `ItemAnimation`; deprecated no-`Registries` overloads dropped |
| `item.component` | **L** | ~60 component records; `AttackRange` reach methods reparameterized over `GameMode`; `Consumable` tick-const via `ServerFlag`→lib |
| `item.enchant` | **L** | `Enchantment(+Impl)`, `LevelBasedValue`, `ValueEffect`, `AttributeEffect`, `EntityEffect`, `ConditionalEffect`, `EffectComponent`, ... |
| `item.armor` / `.book` / `.crossbow` | **L** | `TrimMaterial`, `TrimPattern`, `FilteredText`, `CrossbowChargingSounds` |
| `item.instrument` | **L** | `Instrument(+Impl)`; tick-const via `ServerFlag`→lib |
| `inventory` | **SPLIT** | L: `InventoryType`, `InventoryProperty`. F: `AbstractInventory`, `Inventory`, `PlayerInventory`, `EquipmentHandler`, `InventoryClickHandler`, `TransactionType`, `TransactionOption` |
| `inventory.click` | **SPLIT** | L: `Click`, `ClickType`. F: `ClickPreprocessor`, `InventoryClickProcessor`, `InventoryClickResult` |
| `inventory.type` | **F** | `AnvilInventory`, `BeaconInventory`, `BrewingStandInventory`, `EnchantmentTableInventory`, `FurnaceInventory`, `VillagerInventory` |

### 2.6 World / space / media

| Package | Module | Split line / note |
|---|---|---|
| `coordinate` | **L** | `Vec`, `Pos`, `Point`, `BlockVec`, `ChunkRange`, `Area(+Impl)`, `CoordConversion` |
| `color` / `particle` / `sound` | **L** | `Color`, `AlphaColor`, `DyeColor`; `Particle(+Impl)`, `Particles`; `SoundEvent`, `Music`, `BlockSoundType`, `SoundEvents` |
| `dialog` | **L** | `Dialog` + `Dialog{Action,ActionButton,AfterAction,Body,Input,Metadata}`, `RegistryKeyDialog` |
| `statistic` | **L** | `PlayerStatistic`, `StatisticCategory`, `StatisticType(+Impl)`, `StatisticTypes` |
| `map` (+`framebuffers`) | **L** | `Framebuffer`, `LargeFramebuffer`, `MapColors`, all framebuffers; `MAP_RGB_*` via `ServerFlag`→lib (uses `java.desktop`) |
| `potion` | **L** | `Potion`, `PotionEffect(+Impl)`, `PotionType(+Impl)`, `CustomPotionEffect`, `TimedPotion`, `PotionEffects`, `PotionTypes`; `sendAdd/RemovePacket(Entity)` relocate to Entity/Player |
| `world` (+`biome`, `clock`, `timeline`, `attribute`) | **L** | `DimensionType(+Impl)`, `Difficulty`, `MoonPhase`, `Biome(+Impl)`, `BiomeEffects`, `WorldClock`, `Timeline(+Impl)`, `EnvironmentAttribute*`, `DimensionTypes` |
| `game` / `worldevent` | **L** | `GameEvents`, `WorldEvent` (autogenerated registry data) |
| `collision` | **SPLIT** | L: `BoundingBox`, `Shape(+Impl)`, `SweepResult`, `RayUtils`, `PhysicsResult`, `Aerodynamics`, `BlockBoundingBox`, `PhysicsUtils`, `BlockCollision` block-sweep core, `CollisionUtils` `Block.Getter`/`applyWorldBorder`/`parse*` entry points. F: `EntityCollision(+Result)`, the `Instance`/`Entity`/`Chunk` overloads, `Shape.intersectEntity(Entity)` |

### 2.7 Events / listeners

| Package | Module | Note |
|---|---|---|
| `event` | **F** | `Event`, `EventNode(+Impl,+LazyImpl)`, `GlobalEventHandler`, `EventDispatcher`, `EventFilter`, `EventListener`, `EventBinding`, `EventHandler`, `ListenerHandle` |
| `event.trait` | **F** | `EntityEvent`, `PlayerEvent`, `InstanceEvent`, `BlockEvent`, `ItemEvent`, `InventoryEvent`, `AsyncEvent`, `CancellableEvent`, ... |
| `event.{player,entity(.projectile),instance,inventory,item,book,server}` | **F** | All concrete events wrap live runtime objects |
| `listener` (+`common`, `preplay`, `manager`) | **F** | Entire packet-listener layer |

### 2.8 Adventure / chat / feature managers

| Package | Module | Split line / note |
|---|---|---|
| `adventure` | **SPLIT** | L: `MinestomAdventure`, `BinaryTagHolderImpl`, `ComponentHolder`, `MinestomDataComponentValue(+Impl)`, `AdventurePacketConvertor` (with int-entityId sound overloads). F: `ClickCallbackManager` |
| `adventure.audience` | **F** | `Audiences`, `PacketGroupingAudience`, providers, `AudienceRegistry` |
| `adventure.bossbar` | **F** | `BossBarManager`, `BossBarHolder`, `BossBarListener` |
| `adventure.provider` | **SPLIT** | L: the six pure serializer providers + `NBTLegacyHoverEventSerializer` + `MinestomFlattenerProvider` + `MinestomDataComponentValueConverterProvider`. F: `MinestomClickCallbackProvider`. **`META-INF/services` split alongside** |
| `adventure.serializer.nbt` | **L** | `NbtComponentSerializer(+Impl)`, `NbtDataComponentValue(+Impl)`; Registries via lib holder |
| `message` | **SPLIT** | L: `ChatType(+Impl)`, `ChatTypeDecoration`, `ChatMessageType`, `ChatPosition`, `ChatTypes`. F: `Messenger` |
| `advancements` | **SPLIT** | L: `FrameType`, `AdvancementAction`, `Notification`. F: `Advancement`, `AdvancementRoot`, `AdvancementTab`, `AdvancementManager` |
| `scoreboard` | **F** | `Scoreboard`, `Sidebar`, `Team`, `TeamManager`, `TeamBuilder`, `BelowNameTag`, `TabList`. **Extract `Sidebar.NumberFormat` into a standalone lib type** |
| `recipe` | **SPLIT** | L: `Recipe`, `Ingredient`, `RecipeProperty`, `RecipeBookCategory`, `RecipeType`. F: `RecipeManager` |
| `recipe.display` | **L** | `RecipeDisplay`, `SlotDisplay` |
| `ping` | **L** | `Status`, `ServerListPingType`; online-sampling + LAN-port convenience factories relocate to a framework ping helper |

### 2.9 Top-level runtime, threads, utils

| Package | Module | Split line / note |
|---|---|---|
| `server` (top-level) | **SPLIT** | L: `ServerFlag`, `FeatureFlag(+Impl)`, `FeatureFlags`, `Auth`, `MinecraftConstants` (un-sealed). F: `MinecraftServer`, `ServerProcess(+Impl)`, `Tickable`, `Viewable` |
| `thread` | **F** | `Acquirable(+Impl)`, `Acquired`, `TickThread`, `ThreadDispatcher(+Impl)`, `ThreadProvider`, `TickSchedulerThread`, `MinestomThread` |
| `timer` | **F** | `Scheduler(+Impl)`, `SchedulerManager`, `Task(+Impl)`, `TaskSchedule(+Impl)`, `Schedulable`, `ExecutionType` |
| `monitoring` | **F** | `BenchmarkManager`, `TickMonitor`, `ThreadResult`, `EventsJFR` (uses `jdk.jfr`, `java.management`) |
| `snapshot` | **F** | `Snapshot`, `Snapshotable`, `SnapshotUpdater`, `ServerSnapshot`, `EntitySnapshot`, `ChunkSnapshot`, ... |
| `exception` | **F** | `ExceptionManager`, `ExceptionHandler` |
| `crypto` | **L** | `SignatureValidator` (local logger; `from(Player)` relocated), `ChatSession`, `MessageSignature`, `PlayerPublicKey`, `LastSeenMessages`, `SignedMessageBody`, `ArgumentSignatures`, `FilterMask`, ... |
| `extras.mojangAuth` | **L** | `MojangCrypt` (rethrow locally instead of `ExceptionManager`) |
| `extras.lan` | **F** | `OpenToLAN`, `OpenToLANConfig` |
| `utils` (root) | **SPLIT** | L: `ArrayUtils`, `Direction`, `Ease(Function)`, `Either`, `IntProvider`, `MajorMinorVersion`, `MathUtils`, `ObjectPool`, `Range`, `Rotation`, `StringUtils`, `ThrowingFunction`, `Unit`, `UUIDUtils`, `WeightedList`, `TickUtils`. F: `PacketSendingUtils`, `PacketViewableUtils` |
| `utils.async` | **L** | `AsyncUtils` (complete future exceptionally instead of `ExceptionManager`) |
| `utils.block` | **SPLIT** | L: `BlockUtils`, `BlockIterator` Pos/Vec core. F: `BlockBreakCalculation`, `BlockIterator(Entity,...)` ctors |
| `utils.callback` | **F** | `CommandCallback`, `OptionalCallback` |
| `utils.chunk` | **F** | `ChunkUtils`, `ChunkCache`, `ChunkSupplier`, `ChunkCallback`, `ChunkUpdateLimitChecker` |
| `utils.collection` | **L** | `ObjectArray(+Impl)`, `MappedCollection`, `IntMappedArray`, `AutoIncrementMap`, `MergedMap`, `ConcurrentMessageQueues` |
| `utils.crypto` | **L** | `KeyUtils(+SignatureAlgorithm)` |
| `utils.entity` | **F** | `EntityFinder`, `EntityUtils` |
| `utils.identity` | **L** | `NamedAndIdentified(+Impl)` |
| `utils.inventory` | **L** | `PlayerInventoryUtils` (pure slot math) |
| `utils.json` | **L** | `JsonUtil` |
| `utils.location` | **SPLIT** | L: `RelativeVec` record + `CoordinateType` + `from(Pos)`/`fromView(Pos)`. F: `from(Entity)`/`fromView(Entity)`/`fromSender(CommandSender)` overloads |
| `utils.mojang` | **L** | `MojangUtils` (`AUTH_*` via `ServerFlag`→lib) |
| `utils.nbt` / `.position` / `.time` / `.url` / `.validate` | **L** | `BinaryTag*`; `PositionUtils`; `Tick`/`TimeUnit`/`Cooldown` (`SERVER_TICKS` via `ServerFlag`); `URLUtils`; `Check` |

### 2.10 Command

| Package | Module | Split line / note |
|---|---|---|
| `command` | **SPLIT** | L: `CommandSender`, `ConsoleSender`, `ServerSender`, `ExecutableCommand`, `CommandParser(+Impl)`, `Graph(+Impl)`, `GraphConverter` (after param refactor), `ArgumentParserType`. F: `CommandManager` |
| `command.builder` | **SPLIT** | L: `Command`, `CommandContext`, `CommandData`, `CommandExecutor`, `CommandResult`, `CommandSyntax`, `ParsedCommand`, `SimpleCommand`, `ArgumentCallback`. F: `CommandDispatcher` |
| `command.builder.condition` | **SPLIT** | L: `CommandCondition`, `Conditions.all/any/not`. F: `Conditions.playerOnly/consoleOnly` |
| `command.builder.exception` / `.suggestion` / `.parser` | **L** | `ArgumentSyntaxException`, `IllegalCommandStructureException`; `Suggestion`, `SuggestionCallback`, `SuggestionEntry`; `ArgumentParser`, `CommandQueryResult`, ... |
| `command.builder.arguments` | **SPLIT** | L: `Argument` base + `ArgumentBoolean/Enum/Group/Literal/Loop/String/StringArray/Word` + `ArgumentType` **minus** 4 factory methods. F: `ArgumentCommand` + the `Command()/Entity()/Entities()/ItemStack()/Component()` factories |
| `command.builder.arguments.number` / `.relative` | **L** | `ArgumentNumber`, `ArgumentInteger/Double/Float/Long`; `ArgumentRelativeVec(2/3)`, `ArgumentRelativeBlockPosition` |
| `command.builder.arguments.minecraft` | **SPLIT** | L: `ArgumentBlockState`, `ArgumentColor`, `ArgumentIntRange`, `ArgumentFloatRange`, `ArgumentRange`, `ArgumentNbtTag`, `ArgumentNbtCompoundTag`, `ArgumentResource(+Location,+OrTag)`, `ArgumentTime`, `ArgumentUUID`, `SuggestionType`. F: `ArgumentEntity`, `ArgumentComponent`, `ArgumentItemStack` |
| `command.builder.arguments.minecraft.registry` | **L** | `ArgumentRegistry`, `ArgumentEntityType`, `ArgumentParticle` |

---

## 3. Boundary seams and how each is broken

Seams grouped by kind. Three are **master fixes** that each dissolve a whole family
of crossings at once.

### 3.1 Config (`ServerFlag`) — MASTER FIX

**Seam.** ~15 lib-destined sites read `ServerFlag` constants: `PacketReading`
(`MAX_PACKET_SIZE`, `MAX_PACKET_SIZE_PRE_AUTH`), `PacketWriting` (`MAX_PACKET_SIZE`),
`PacketVanilla` (`POOLED_BUFFER_SIZE`), `NetworkBufferTemplate` (`TEMPLATE_COMPILER`),
`CachedPacket` (`CACHED_PACKET`), `tag.*` (`SERIALIZE_EMPTY_COMPOUND`,
`TAG_HANDLER_CACHE_ENABLED`), `registry` (`REGISTRY_UNSAFE_OPS`, `INSIDE_TEST`),
`ClientSettings` (`CHUNK_VIEW_DISTANCE`), `WorldBorder` (`WORLD_BORDER_SIZE`),
`Consumable`/`Instrument` (`SERVER_TICKS_PER_SECOND`), `MapColors` (`MAP_RGB_*`),
`ConcurrentMessageQueues` (`UNSAFE_COLLECTIONS`), `MojangUtils` (`AUTH_*`),
`MinestomFlattenerProvider` (`AUTOMATIC_COMPONENT_TRANSLATION`).

**Fix.** Move `ServerFlag.java` **wholesale into `:lib`**, package unchanged
(`net.minestom.server`). It is a pure leaf with zero framework references. All 38
referencing files compile unchanged; every crossing becomes a legal `lib→lib` or
`framework→lib` read. Also derive `MinecraftServer.TICK_MS`-dependent lib code
(`TickUtils`, `utils.time.Tick`) from `ServerFlag.SERVER_TICKS_PER_SECOND` (identical
value). **Not a break.**

### 3.2 Config (version/protocol constants) — MASTER FIX

**Seam.** `PROTOCOL_VERSION` / `VERSION_NAME` / `DATA_VERSION` /
`RESOURCE_PACK_VERSION` / `DATA_PACK_VERSION` are read by lib
`SelectKnownPacksPacket.MINECRAFT_CORE`, `ping.Status.VersionInfo.DEFAULT`, and
`Scratch`. They live on the autogenerated `MinecraftConstants`, declared
`sealed interface MinecraftConstants permits MinecraftServer` — the `permits` clause
pins it to framework.

**Fix.** Relocate `MinecraftConstants` into `:lib` as a **plain (un-sealed) interface**
(it already imports only lib `MajorMinorVersion`). Framework `MinecraftServer` keeps
`implements MinecraftConstants`, so `MinecraftServer.PROTOCOL_VERSION` still resolves
by constant inheritance (no caller change). Lib types reference
`MinecraftConstants.PROTOCOL_VERSION` directly. `Scratch` repoints its static import.
**Not a break** (framework consumers unchanged).

### 3.3 Service-locator: `MinecraftServer.process()` as `Registries` provider — MASTER FIX

**Seam.** Lib serialization code fetches the running server's `Registries` via
`MinecraftServer.process()`: `tag.Serializers` (`ITEM`, `COMPONENT`),
`adventure.NbtComponentSerializerImpl`, `MinestomDataComponentValueConverterProvider`
(a no-arg ServiceLoader SPI), and the deprecated `ItemStack` NBT overloads.

**Fix.** Add a **lib default-`Registries` holder**: a settable static
(`Registries.staticRegistries()`) defaulting to `Registries.vanilla()`; framework
points it at the live `ServerProcess` on startup. `Registries` is already a lib
interface implemented by `ServerProcess`. No-arg / singleton / SPI sites read the
holder instead of the static accessor. **Not a break.**

Two related sites resolved by the same pattern:
- `PacketVanilla.PACKET_POOL` builds its pooled `staticBuffer` with
  `MinecraftServer.process()`. **Fix:** construct with neutral registries —
  `NetworkBuffer.staticBuffer(ServerFlag.POOLED_BUFFER_SIZE)`;
  `PacketReading.readFramedPacket` already overwrites the pooled buffer's registries
  per read (`decompressed.registries(buffer.registries())`). **Not a break.**
- `CachedPacket.updatedCache` reads `MinecraftServer.getCompressionThreshold()`.
  **Fix:** read a lib-level compression-threshold holder (framework updates it) rather
  than injecting a constructor param. **Not a break.**

### 3.4 Service-locator: registry lifecycle, exceptions, logging, tag invalidation

Each replaced by a lib hook; all internal, none breaking:

| Site | Today | Lib fix |
|---|---|---|
| `DynamicRegistryImpl.isFrozen()` | `MinecraftServer.process()/isStarted()` | lib freeze-state hook (`BooleanSupplier`), default never-frozen; framework flips it true at startup |
| `RegistryData.load()` | `MinecraftServer.getExceptionManager()` | rethrow unchecked / lib SLF4J logger |
| `RegistryTagImpl.Backed.invalidate()` | `MinecraftServer.process().connection().invalidateTags()` | lib tag-invalidation callback (no-op default); framework registers a listener |
| `MojangCrypt` (5 sites) | `MinecraftServer.getExceptionManager()` | rethrow unchecked / local logger |
| `AsyncUtils.runAsync` | `MinecraftServer.getExceptionManager()` | complete the returned future exceptionally |
| `SignatureValidator` (2 sites) | `MinecraftServer.LOGGER` | private static `LoggerFactory.getLogger(...)` |

### 3.5 Service-locator: command layer

- `GraphConverter` reaches `CommandManager` and takes a `Player`. **Fix:** drop the
  `CommandManager` param (pass the console `CommandSender` + the `CommandParser`);
  change `Player` param to `CommandSender`. Package-private, **not a break**.
- `ArgumentComponent`/`ArgumentItemStack` call `MinecraftServer.process()` for a
  `RegistryTranscoder`. **Fix (chosen, non-breaking):** keep them in `:framework`; the
  framework `ArgumentType` factory injects the live `Registries`.
- `ArgumentCommand` dispatches against the live `CommandManager` — inherently runtime,
  stays framework.

### 3.6 Instance-runtime seams

| Seam | Fix | Break? |
|---|---|---|
| `BlockHandler` callback context holds live `Instance`/`Player`/`Entity` | Split into lib identity super-interface (`getKey`/`getBlockEntityTags`/`getBlockEntityAction`/`isTickable`, stored by `Block`) + framework callback sub-interface (`onPlace/…/tick` + `Placement`/`Destroy`/`Interaction`/`Touch`/`Tick`) | **Yes** |
| `Heightmap` abstract base is `Chunk`-bound | Extract `Type` + static `encode()` to a lib type; leave the `Chunk`-attached base + subclasses in framework | **Yes** |
| `Light.getNeighbors(Chunk,int)` | Relocate the `@Internal` helper to framework `LightingChunk`; lib `Light` keeps `calculate*` over `LightLookup`/`PaletteLookup` | No |
| `CollisionUtils`/`BlockCollision` mix `Block.Getter` core + `Instance`/`Entity` overloads; `EntityCollision(+Result)` wrap live `Entity`/`EntityTracker` | Keep `CollisionUtils` public entry class in framework for runtime overloads; extract `Block.Getter` sweep core + `BoundingBox`/`Shape(+Impl)`/`PhysicsResult`/`Aerodynamics`/`PhysicsUtils`/`applyWorldBorder`/`parse*` into lib | No |

### 3.7 Entity-runtime seams (the amputations)

| Seam | Fix | Break? |
|---|---|---|
| `EntityMeta` holds `WeakReference<Entity>` + `consumeEntity`, threading `import Entity` through ~130 subclass ctors; 6 subclasses call `getBoundingBox()/setBoundingBox()` | Hoist a minimal lib `MetaTarget` interface (`getBoundingBox`/`setBoundingBox`); `EntityMeta` holds `WeakReference<MetaTarget>` + `consumeEntity(Consumer<MetaTarget>)`; `Entity implements MetaTarget` | **Yes** |
| `MetadataHolder(@Nullable Entity)` deprecated ctor | Drop it; keep the `Consumer<Map<…>>` primary ctor; framework passes `entity::notifyMetadataChanges` | **Yes (low)** |
| `AttributeInstance.clearModifiers()` reads `LivingEntity.PROTECTED_MODIFIERS` | Move `PROTECTED_MODIFIERS` (+ `SPRINTING_SPEED_MODIFIER` key) into lib; `LivingEntity` may keep an alias | **Yes (low)** |
| `Shape.intersectEntity(Point,Entity)` default on lib interface | Reparameterize to `intersectEntity(Point,Point,BoundingBox)` | **Yes** |
| `WorldBorder.inBounds(Entity)` | Keep `inBounds(Point)` in lib; drop/relocate the Entity overload | **Yes (low)** |
| `AttackRange.effectiveMin/MaxReach(Entity)` branch on `Player.getGameMode()` | Reparameterize over lib `GameMode` (`+ boolean isPlayer`) | **Yes (low)** |
| `Potion.sendAdd/RemovePacket(Entity)` | Relocate onto `Entity`/`Player` in framework | **Yes (low)** |
| `AdventurePacketConvertor.create*SoundPacket(Entity)` use `getEntityId()` | Add int-entityId overload in lib; leave Entity overloads in framework | **Yes (low)** |
| `SignatureValidator.from(Player)` | Keep `from(PublicKey)`/`from(PlayerPublicKey)` in lib; relocate `from(Player)` to framework | **Yes (low)** |
| `BlockIterator(Entity,int)/(Entity)` ctors | Drop from lib (Pos + direction Vec ctors exist); optional framework helper | **Yes (low)** |
| `RelativeVec.from(Entity)/fromView(Entity)/fromSender(CommandSender)` | Keep record + `from(Pos)`/`fromView(Pos)` in lib; move Entity/Sender overloads to a framework util | **Yes (low)** |
| `Conditions.playerOnly()/consoleOnly()` do `instanceof Player` | Move to a framework condition helper; keep `CommandCondition` + `all/any/not` in lib | **Yes (low)** |
| `ItemStack` deprecated no-`Registries` `fromItemNBT/toItemNBT/Hash.of` | Drop the deprecated overloads, or route through the lib holder (3.3) | **Yes (low)** — softenable to non-break |
| `ping.Status.PlayerInfo.onlineCount()/online()` + `ServerListPingType.getOpenToLANPing()` | Keep records + `CODEC` + formatter in lib; relocate the online-sampling / LAN-port factories to a framework ping helper | **Yes (low)** |

### 3.8 Type-extraction seams (nested-in-framework data)

- **`Sidebar.NumberFormat`** is a pure `FormatType` + optional `Component` +
  `SERIALIZER` record, but nested in the framework `Sidebar` (imports `Player`). Lib
  `UpdateScorePacket`/`ScoreboardObjectivePacket` reference it. **Fix:** extract
  `NumberFormat` into a standalone lib type; both lib packets and framework `Sidebar`
  reference it. Without this, `network.packet.server.play` cannot compile in `:lib`.
  **Break (medium)** — `Sidebar` may keep a re-export alias.
- **`ArgumentType`** factory constructs framework-bound args. **Fix:** move
  `ArgumentType.Command()/Entity()/Entities()/ItemStack()/Component()` to a
  framework-side factory; lib `ArgumentType` keeps all structural/data factories.
  **Break (high, pervasive)**.

### 3.9 Javadoc-only imports (mechanical, non-breaking)

Drop the framework import and demote `{@link}`/`{@see}` to plain text or fully-qualified
references: `ServerPacket`/`SendablePacket`→`PlayerConnection`;
`Block`→`Instance`/`Batch`; `DynamicRegistry`→`Player`; `ClientSettings`→`Player`;
`PlayerStatistic`→`Player`; `Notification`→`Player`; `CommandSender`→`Player`;
`CommandSyntax`→`Player`/`CommandManager`; `ServerListPingType`→`ServerListPingEvent`/
`OpenToLAN`; and the `banner`/`jukebox`/`dialog`/`world` `@see MinecraftServer`
references. **Not breaks.**

### 3.10 Packaging seam (ServiceLoader)

The root `module-info` `provides` directives + `META-INF/services` files must be
partitioned: `:lib` ships the six pure serializer providers + `MinestomFlattenerProvider`
+ `MinestomDataComponentValueConverterProvider`; `:framework` ships
`MinestomClickCallbackProvider`. Packaging change only, **not a break**.

### 3.11 Confirmed clean (no crossings)

No lib-destined type references `event.*`, `timer.*`, `thread.*`, `monitoring.*`,
`snapshot.*`, or the inventory runtime. The two lib inventory types (`InventoryType`,
`InventoryProperty`) reference no framework anchor. Nothing to break there.

---

## 4. Breaking changes — ranked, minimal

**Total public breaks: 3 high, 3 medium, ~11 low.** Everything else (the three master
fixes, all holders/hooks, javadoc, packaging, `GraphConverter`, `Light.getNeighbors`,
collision split, `CachedPacket` threshold, pooled-buffer registries) is **non-breaking
by construction.**

### 4.1 Unavoidable (public API changes intrinsic to the split)

1. **[HIGH] Split `BlockHandler`** into a lib identity super-interface + framework
   callback sub-interface. *Why unavoidable:* the callback context records structurally
   hold live `Instance`/`Player`/`Entity`; `Block` (lib) must still store a handler
   reference. *Migration:* custom handlers overriding `onPlace/onDestroy/onInteract/
   onTouch/tick` implement the framework sub-interface; `Block.withHandler()/handler()`
   re-type to the lib super-interface (a widening — existing instances stay assignable).
2. **[HIGH] Decouple `EntityMeta` from `Entity` via lib `MetaTarget`.** *Why unavoidable:*
   the ~130-class metadata tree is lib-destined and every subclass ctor threads `Entity`.
   *Migration:* ctor param `Entity`→`MetaTarget`; call sites pass the `Entity` unchanged
   (it implements `MetaTarget`); code declaring the param type or subclassing a meta
   switches to `MetaTarget`.
3. **[HIGH] Move `ArgumentType.Command()/Entity()/Entities()/ItemStack()/Component()`
   to a framework factory.** *Why unavoidable:* those args are framework-bound
   (`CommandManager`/`EntityFinder`/live `Registries`). *Migration:* command definitions
   using these five factories import the framework `ArgumentType` factory; all other
   `ArgumentType.*` factories unchanged.
4. **[MEDIUM] Extract `Sidebar.NumberFormat` to a standalone lib type.** *Migration:*
   references retarget to the new FQN; `Sidebar` keeps a re-export alias to soften.
5. **[MEDIUM] Extract `Heightmap.Type` + `encode()` to a lib class**; the `Chunk`-bound
   base + subclasses stay framework. *Migration:* users of `Type`/`encode` point at the
   lib class; users of the base point at framework (FQN unchanged if the base keeps the
   `Heightmap` name).
6. **[MEDIUM] `Shape.intersectEntity(Point,Entity)` → `intersectEntity(Point,Point,
   BoundingBox)`.** *Migration:* callers pass `entity.getPosition()` +
   `entity.getBoundingBox()`; custom `Shape` impls override the new signature.

### 4.2 Avoidable / softenable (take only if cheap; otherwise defer via holder or alias)

- **[MEDIUM] Inject `Registries` into `ArgumentComponent`/`ArgumentItemStack`** to pull
  them into lib. *Deferred:* keep them in `:framework` — **zero break**.
- **[MEDIUM] Drop deprecated no-`Registries` `ItemStack` overloads.** *Softenable:*
  route through the lib default-`Registries` holder (3.3) — **zero break**; remove later
  as a deprecation cycle.
- **[LOW, batch] Amputated convenience overloads** (`Potion.send*`,
  `WorldBorder.inBounds(Entity)`, `AttackRange` reach, `BlockIterator(Entity)`,
  `RelativeVec.from(Entity)/fromSender`, `SignatureValidator.from(Player)`,
  `Conditions.playerOnly/consoleOnly`, ping online/LAN factories,
  `AdventurePacketConvertor` Entity sound overloads, `MetadataHolder(Entity)` ctor). Each
  is a small mechanical relocation to a framework helper/method. Where churn matters,
  leave a `@Deprecated` framework-side shim.
- **[LOW] `PROTECTED_MODIFIERS` canonical home moves to lib.** *Softenable:* `LivingEntity`
  keeps a public alias — **zero caller churn**.

### 4.3 Explicitly not breaks (to reassure API consumers)

`ServerFlag` and `MinecraftConstants` relocations (package unchanged; framework inherits),
the lib default-`Registries`/freeze/tag-invalidation/logger/compression holders,
`PacketVanilla` neutral pooled registries, `GraphConverter` package-private refactor,
`Light.getNeighbors` relocation, collision core extraction, and all javadoc `{@link}`
demotions. `MinecraftServer.PROTOCOL_VERSION`, `MinecraftServer.getCompressionThreshold()`,
and existing `ServerFlag.X` references all still resolve unchanged.

---

## 5. Gradle wiring

### 5.1 Namespace and JPMS — the decision

**Current reality (decisive).** The engine is one JPMS module today:
`src/main/java/module-info.java` declares `module net.minestom.server` exporting ~180
packages; `minestom.java-library` sets `modularity.inferModulePath = true`; `demo`
(`module net.minestom.demo`) and `testing` (`module net.minestom.testing`) both
`requires net.minestom.server`.

**The constraint.** JPMS forbids two modules from sharing a package (split packages).
The plan keeps `net.minestom.server.*` FQNs and produces **~24 split packages**
(`server`, `network`, `network.player`, `entity`, `entity.damage`, `instance`,
`instance.block`, `instance.heightmap`, `inventory`, `inventory.click`, `collision`,
`adventure`, `adventure.provider`, `message`, `advancements`, `recipe`, `command`,
`command.builder`, `command.builder.condition`, `command.builder.arguments`,
`command.builder.arguments.minecraft`, `utils`, `utils.block`, `utils.location`). Two
separately `requires`-able named modules with this layout **cannot exist** — not by
authoring `module-info`, and not by placing `:lib` as an automatic module on the module
path (still a split package).

> Note: module *name* is independent of package *names*. A `net.minestom.lib` module
> can legally export `net.minestom.server.*` packages — the only rule is that each exact
> package string belongs to exactly one module. So pure (non-split) packages carry **zero
> import changes** regardless of which module owns them; only the ~24 genuinely-mixed
> packages force the surgery in Section 4.

**Recommendation: keep `net.minestom.server.*` in both modules; ship `:lib` and
`:framework` as non-modular (classpath) jars; preserve JPMS for module-path consumers via
a single aggregate artifact.**

1. **Namespace: keep `net.minestom.server.*` in both modules.** Moving `:lib` to
   `net.minestom.lib.*` (Option B) would rename roughly half the public API and break
   every downstream import — a far larger break than all of Section 4 combined.
2. **`:lib` and `:framework` carry no `module-info.java`** and set
   `modularity.inferModulePath = false`. Split packages across two classpath jars are
   legal. Add `Automatic-Module-Name` manifest entries (`net.minestom.lib`,
   `net.minestom.server`), documenting that the two jars must not both sit on a downstream
   **module** path simultaneously.
3. **Preserve JPMS with an aggregate artifact.** Keep publishing today's `minestom`
   coordinate as an **aggregate jar** = `:lib` + `:framework` classes together, carrying
   the existing `module-info.java` (`module net.minestom.server`, all exports +
   `provides`). Module-path consumers (and `demo`/`testing` via `requires
   net.minestom.server`) depend on the aggregate and are unaffected. Classpath consumers
   who want only structs depend on `:lib`.

**Rejected alternative (Option B, deferred).** `:lib` → `net.minestom.lib.*`,
`:framework` stays `net.minestom.server.*`, two clean `requires`-able JPMS modules, no
split packages. Architecturally cleanest, but renames every lib type (~800 files) and
breaks all downstream imports. Revisit only for a major-version rename.

### 5.2 `settings.gradle.kts`

```kotlin
rootProject.name = "minestom"

includeBuild("build-src")

include("lib")
include("framework")

include("code-generators")
include("testing")
include("jmh-benchmarks")
include("jcstress-tests")
include("demo")
```

The root `build.gradle.kts` stops carrying `src/main/java`; it becomes the **aggregate**
module (depends on `:lib` + `:framework`, holds the shared `module-info.java`, keeps
blossom + publishing + graalvm). Keeping the aggregate at the root preserves the `minestom`
artifact id and the existing publishing/nmcp/graalvm blocks with least disruption.

### 5.3 `:lib/build.gradle.kts`

```kotlin
plugins { id("minestom.java-library") }

dependencies {
    api(libs.bundles.adventure)   // NetworkBuffer/codecs/components are kyori-typed
    api(libs.gson)                // codecs, JSON transcoder, JsonUtil
    implementation(libs.fastutil) // palettes, StaticIntMap, collections
    implementation(libs.jcTools)  // ConcurrentMessageQueues
    implementation(libs.slf4j)    // lib loggers (SignatureValidator, RegistryData, MojangCrypt)
    implementation(libs.minestomData) // RegistryData JSON loader
    // java.desktop (map framebuffers) — JDK module, no external dep
}

java { modularity.inferModulePath = false }
```

`:lib` receives the lib slice of `src/autogenerated/java` (registry data +
`MinecraftConstants` + `FeatureFlags`).

### 5.4 `:framework/build.gradle.kts`

```kotlin
plugins { id("minestom.java-library") }

dependencies {
    api(project(":lib"))          // re-exports adventure + gson transitively
    implementation(libs.fastutil) // thread/instance/entity-tracker
    implementation(libs.bundles.flare)   // EntityTrackerImpl, InstanceContainer only
    implementation(libs.jcTools)  // scheduler/thread queues
    implementation(libs.slf4j)
    // jdk.jfr (EventsJFR), java.management (BenchmarkManager) — JDK modules
}

java { modularity.inferModulePath = false }
```

### 5.5 Dependency reassignment

| Dependency | Today | `:lib` | `:framework` | Evidence |
|---|---|---|---|---|
| adventure bundle | `api` | **`api`** | inherited | NetworkBuffer/codecs/components/serializers |
| gson | `api` | **`api`** | inherited | codec JSON transcoder, `JsonUtil`, `utils.mojang` |
| fastutil | `impl` | **`impl`** | **`impl`** | palettes/collections (lib) + tracking (fw) |
| jctools | `impl` | **`impl`** | **`impl`** | `ConcurrentMessageQueues` (lib) + scheduler (fw) |
| slf4j | `impl` | **`impl`** | **`impl`** | lib loggers + framework logging |
| jetbrains annotations | convention | **`api`** | inherited | via `minestom.java-library` |
| minestomData | `impl` | **`impl`** | — | only `registry.RegistryData` imports `net.minestom.data` |
| flare bundle | `impl` | — | **`impl`** | only `EntityTrackerImpl`, `InstanceContainer` |
| `jdk.jfr` (JDK) | module req | — | **framework** | only `monitoring.EventsJFR` |
| `java.management` (JDK) | module req | — | **framework** | only `monitoring.BenchmarkManager` |
| `java.desktop` (JDK) | module req | **lib** | inherited | `map.framebuffers.Graphics2DFramebuffer` |

Root aggregate keeps the union (unchanged from today's `build.gradle.kts`).

### 5.6 Convention plugins

`minestom.java-library` currently forces `modularity.inferModulePath = true`. For
`:lib`/`:framework` (no `module-info`) set `modularity.inferModulePath = false` in the two
module builds (or add a `minestom.java-classpath` variant). The aggregate/root keeps
inference on (it has the `module-info`). Everything else in the convention (Java 25
toolchain, sources/javadoc jars, JUnit, `-Dminestom.*` test props) applies unchanged.

### 5.7 Autogenerated sources, blossom, code-generators

- `src/autogenerated/java` splits by the same rules: nearly all of it is registry **data**
  → `:lib` (`Materials`, `Blocks`, `EntityTypes`, `SoundEvents`, `Particles`,
  `DimensionTypes`, `ChatTypes`, `PotionTypes`, `Attributes`, `RecipeBookCategory`,
  `ArgumentParserType`, `StatisticTypes`, `GameEvents`, `WorldEvent`, `DyeColor`,
  `FeatureFlags`, `MinecraftConstants`). Point `code-generators`' output at `:lib`'s
  `src/autogenerated/java` srcDir. If any generated class is framework-only, route just
  that file to `:framework` (none identified).
- **blossom** (COMMIT/BRANCH/GROUP/ARTIFACT/VERSION substitution) targets a build-metadata
  source that is a framework/aggregate concern; no blossom-templated file was found under
  lib-destined packages, so blossom stays on the **aggregate/root** (confirm the templated
  file's location during migration).
- **ServiceLoader**: create `META-INF/services` files — the six serializer providers +
  `MinestomFlattenerProvider` + `MinestomDataComponentValueConverterProvider` in `:lib`,
  `MinestomClickCallbackProvider` in `:framework`. The aggregate `module-info` retains the
  union of `provides` directives.

### 5.8 Re-pointing demo / testing / jmh / jcstress

- **demo**: keep `requires net.minestom.server` and depend on the **aggregate**
  (`implementation(project(":"))`). Move **`Scratch.java`** into a dedicated `:lib`-only
  compile surface (a `scratch` example source set in `:lib`, or a tiny `:scratch` project
  with `implementation(project(":lib"))` and no `module-info`) so CI proves lib-closure.
  Repoint Scratch's `import static …MinecraftServer.PROTOCOL_VERSION` to
  `MinecraftConstants.PROTOCOL_VERSION`.
- **testing**: keep `requires net.minestom.server`; depend on the aggregate
  (`api(project(":"))`).
- **jmh-benchmarks / jcstress-tests**: default to the aggregate to avoid triage; migrate
  hot lib-only benchmarks to `:lib` opportunistically.

### 5.9 Publishing / ABI

- `minestom.publishing` currently publishes `rootProject` and `:testing`. Add `:lib` and
  `:framework` publications (e.g. `net.minestom:lib`, `net.minestom:framework`) and keep
  the aggregate `net.minestom:minestom`. Wire all four into `nmcpAggregation`.
- `CheckAbiTask` baselines per artifact by `project.name`. Establish fresh baselines for
  `:lib`/`:framework` at the first post-split release; the aggregate keeps comparing
  against the historical `minestom.jar` baseline (its ABI is the union, stable modulo
  Section 4).

---

## 6. Migration sequence (green at each stage)

**Strategy: do every decoupling refactor in-place while it is still one module and one
green test suite, then physically split.** No stage leaves the tree red.

- **Stage 0 — Baseline.** Full build + tests green. Snapshot the current `module-info`
  exports and ABI baseline.
- **Stage 1 — Master config fixes (in place, non-breaking).** Confirm `ServerFlag` has
  zero framework refs. Un-seal `MinecraftConstants` (keep `MinecraftServer implements
  MinecraftConstants`). Repoint lib `TICK_MS` derivations onto
  `ServerFlag.SERVER_TICKS_PER_SECOND`.
- **Stage 2 — Lib holders and hooks (in place, non-breaking).** Introduce the lib
  default-`Registries` holder, registry freeze-state hook, tag-invalidation hook,
  compression-threshold holder, and local loggers. Repoint `tag.Serializers`,
  `NbtComponentSerializerImpl`, `MinestomDataComponentValueConverterProvider`,
  `DynamicRegistryImpl`, `RegistryData`, `RegistryTagImpl`, `MojangCrypt`, `AsyncUtils`,
  `SignatureValidator`, `CachedPacket`, `PacketVanilla.PACKET_POOL` off the
  `MinecraftServer` statics. Wire framework startup to set the holders.
- **Stage 3 — Non-breaking structural refactors.** `GraphConverter` param change;
  `Light.getNeighbors` relocation to `LightingChunk`; collision core extraction; javadoc
  `{@link}`/`{@see}` demotions across all lib-destined files.
- **Stage 4 — Type extractions.** Extract `Sidebar.NumberFormat` (keep `Sidebar` alias);
  extract `Heightmap.Type`+`encode()` (keep the framework base named `Heightmap`);
  relocate `PROTECTED_MODIFIERS` into lib (keep `LivingEntity` alias).
- **Stage 5 — Entity/runtime decoupling (the breaks).** Introduce lib `MetaTarget`;
  retype `EntityMeta`/subclass ctors `Entity`→`MetaTarget`; `Entity implements MetaTarget`.
  Split `BlockHandler`; retype `Block.withHandler/handler`. Reparameterize
  `Shape.intersectEntity`. Amputate the convenience overloads. Split `ArgumentType`
  factory. Update all in-tree callers (demo, testing).
- **Stage 6 — Enforce the boundary with a grep gate.** Assert no lib-destined file imports
  a framework anchor:
  ```
  grep -rlE "import net\.minestom\.server\.(MinecraftServer|ServerProcess|event\.|listener\.|thread\.|timer\.|monitoring\.|snapshot\.|exception\.)" <lib-destined dirs>
  ```
  plus targeted checks for `instance.Instance|Chunk|EntityTracker`,
  `entity.Entity|Player|LivingEntity` (excluding metadata/enums), and
  `inventory.AbstractInventory`. Must return empty. This gate is the go/no-go for the move.
- **Stage 7 — Create `:lib` skeleton.** Add `lib/build.gradle.kts`,
  `modularity.inferModulePath=false`, no `module-info`. Leave it empty and building.
- **Stage 8 — Physically move lib files.** Move the lib-verdict files/subtrees (Section 2)
  into `:lib/src/main/java`, including the lib slice of `src/autogenerated/java`,
  `ServerFlag`, `MinecraftConstants`, `FeatureFlag(+Impl)`, `Auth`, and the lib
  `META-INF/services`. Repoint `code-generators` output to `:lib`. Build `:lib` alone;
  run lib-scoped tests green.
- **Stage 9 — Create `:framework`.** Move remaining files into `:framework/src/main/java`;
  `api(project(":lib"))` + framework deps. Framework startup sets the lib holders. Build
  `:framework`; run framework tests green.
- **Stage 10 — Aggregate + JPMS.** Turn the root into the aggregate (`:lib` + `:framework`,
  retains `module-info.java`, blossom, publishing, graalvm). Verify `demo`/`testing`
  `requires net.minestom.server` unchanged. Add `Automatic-Module-Name` to
  `:lib`/`:framework`. Full test suite green.
- **Stage 11 — Prove Scratch closure.** Add the `:lib`-only Scratch compile surface; it must
  compile and run against `:lib` alone. Repoint its `PROTOCOL_VERSION` import. CI job:
  `:lib:compileScratch` (or `:scratch:build`).
- **Stage 12 — Publishing + ABI + cleanup.** Add `:lib`/`:framework` publications and nmcp
  wiring; set fresh ABI baselines; migrate opportunistic lib-only jmh/jcstress/testing to
  `:lib`. Remove dead `@Deprecated` shims at the next major.

---

## 7. Risks and open questions

1. **JPMS split-package (highest risk / primary decision).** The plan keeps
   `net.minestom.server.*` and creates ~24 split packages, incompatible with two
   `requires`-able named modules. Mitigation (5.1): non-modular `:lib`/`:framework` jars +
   an aggregate module keeping today's `module-info`. **Open question:** does Minestom
   require standalone module-path consumption of `:lib` *alone*? If yes, escalate to
   Option B (`net.minestom.lib.*` rename) as a major-version item. Confirm with downstream
   before committing.
2. **Global mutable lib state** (default-`Registries` holder, freeze-state, compression,
   tag-invalidation). Replacing per-call service-locator with settable statics reintroduces
   global-singleton coupling in a lib package. Risks: test isolation, initialization order
   (a lib call before framework sets the holder gets `Registries.vanilla()` — usually
   correct), thread visibility. Mitigate with `volatile`/`AtomicReference` holders, a
   documented "framework installs on boot" contract, and test-reset hooks. **Open question:**
   settable global vs. threading `Registries` explicitly (larger but cleaner)?
3. **`ArgumentComponent`/`ArgumentItemStack` placement.** Kept in `:framework` for zero
   break (they need runtime `Registries`). **Open question:** is command-arg parsing in
   scope for lib closure? Scratch does not need it.
4. **Autogenerated + blossom routing.** Nearly all `src/autogenerated/java` is lib data, but
   validate file-by-file (a stray framework reference in a generated class would break
   `:lib`). Confirm the blossom-templated file's exact path and keep it framework-side.
5. **ServiceLoader correctness after split.** With `:lib`/`:framework` non-modular, ensure
   the `META-INF/services` files are present and the aggregate `module-info` lists all
   `provides`. A missing registration silently disables a serializer. Verify with an
   integration test resolving each Adventure provider.
6. **Dependency locality.** flare / jfr / java.management are framework-only; java.desktop
   is lib (map). Confirmed by grep. The map package pulling `java.desktop` into `:lib`
   slightly widens lib's JDK surface — acceptable (headless servers already load it).
7. **ABI baselines across three artifacts.** New baselines for `:lib`/`:framework`; the
   aggregate's ABI should equal today's minus the Section-4 breaks. First post-split release
   reports the intended breaks — coordinate a changelog/deprecation note.
8. **Demo split-brain.** `demo` keeps `Main` on the aggregate but `Scratch` on `:lib` alone.
   Model this cleanly (separate source set or module) so a stray framework import in
   `Scratch` fails fast rather than compiling against the aggregate by accident.
