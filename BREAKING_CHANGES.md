# Module split (`:lib` / `:framework`) — breaking changes

Minestom is now three artifacts:

| Artifact | Contents | Use it when |
|---|---|---|
| `net.minestom:minestom` | Aggregate of `:lib` + `:framework`, single modular jar (`module net.minestom.server`) | Existing users — **drop-in replacement**, JPMS-safe |
| `net.minestom:lib` | Unopinionated protocol/data: packets, `NetworkBuffer`, codecs, registries, coordinates, blocks/items/entities *data*, command graph structs | Custom servers built directly on the packet/data API (see `demo/src/scratch/.../Scratch.java`) |
| `net.minestom:framework` | Opinionated runtime: `MinecraftServer`, `ServerProcess`, events, listeners, tick loop, live `Instance`/`Entity`/`Player`/`Inventory`, scheduler, threads | Depends on `:lib`; not usable alone |

Packages keep their `net.minestom.server.*` names in both modules. Because ~24 packages
are split across the two jars, `:lib` and `:framework` are **classpath jars** (no
`module-info`): never put them both on the JPMS module path — module-path consumers use
the aggregate.

Most user code compiles unchanged. The exhaustive list of API changes follows, worst first.

## High

1. **`BlockHandler` split.** New lib super-interface
   `net.minestom.server.instance.block.BlockDataHandler` carries the identity surface
   (`getKey()`, `isTickable()`, `getBlockEntityTags()`, `getBlockEntityAction()`);
   `BlockHandler extends BlockDataHandler` keeps the callbacks
   (`onPlace`/`onDestroy`/`onInteract`/`onTouch`/`tick`, the nested context records, `Dummy`).
   `Block.withHandler(...)` now accepts `BlockDataHandler` (widening — existing handlers
   still assignable) and `Block.handler()` returns `@Nullable BlockDataHandler`.
   *Migration:* handler implementations are unaffected. Callers that stored
   `Block.handler()` in a `BlockHandler` variable declare `BlockDataHandler` instead, or
   narrow with `instanceof BlockHandler` when invoking callbacks.

2. **`EntityMeta` decoupled from `Entity` via `MetaTarget`.** New lib interface
   `net.minestom.server.entity.MetaTarget` (`getEntityId()`, `getBoundingBox()`,
   `setBoundingBox(double, double, double)`); `Entity implements MetaTarget`. Every
   metadata class constructor takes `@Nullable MetaTarget` instead of `@Nullable Entity`,
   and `EntityMeta.consumeEntity` consumes `MetaTarget`.
   *Migration:* call sites passing an `Entity` compile unchanged. Code declaring the
   parameter type, subclassing a meta, or reading entity-valued meta getters
   (`getShooter()` etc., now `MetaTarget`-typed) switches to `MetaTarget` (cast back to
   `Entity` where the runtime object is known to be one).

3. **Server-bound `ArgumentType` factories moved.** `ArgumentType.Command()`, `.Entity()`,
   `.Entities()`, `.ItemStack()`, `.Component()` now live on
   `net.minestom.server.command.builder.arguments.ServerArgumentType` (same package,
   identical signatures).
   *Migration:* mechanical rename / static-import swap. All other `ArgumentType.*`
   factories are unchanged. Related: the `"entity"`, `"entities"`, `"player"`,
   `"players"`, `"itemstack"`, `"component"`, `"command"` identifiers of
   `ArgumentType.generate(format)` are registered by the framework at class-load of
   `ServerArgumentType`/server init — they are unavailable to `:lib`-only consumers.

## Medium

4. **`Sidebar.NumberFormat` → top-level `net.minestom.server.scoreboard.NumberFormat`.**
   Same members. No alias possible (record). *Migration:* import swap.

5. **`Heightmap.Type` + `Heightmap.encode(short[], int)` → new lib enum
   `net.minestom.server.instance.heightmap.HeightmapType`** (same constants; `encode` is a
   static on the enum). The `Chunk`-bound `Heightmap` base + subclasses stay in the
   framework. *Migration:* `Heightmap.Type.X` → `HeightmapType.X`,
   `Heightmap.encode(...)` → `HeightmapType.encode(...)`.

6. **`Shape.intersectEntity(Point, Entity)` →
   `intersectEntity(Point, Point entityPosition, BoundingBox entityBoundingBox)`.**
   *Migration:* pass `entity.getPosition()`, `entity.getBoundingBox()`; custom `Shape`
   implementations adopt the new signature.

7. **`Packet` is no longer `sealed`.** Cross-package sealing (`permits ClientPacket,
   ServerPacket`) requires a named module, and `:lib` is a classpath jar. The
   `ClientPacket`/`ServerPacket` hierarchies themselves remain sealed. *Migration:*
   exhaustive switches over `Packet` (rare) need a `default` arm.

## Low (amputated framework conveniences)

8. `Potion.sendAddPacket(Entity)` / `sendRemovePacket(Entity)` removed (logic lives in
   `Entity`'s effect methods). *Migration:* use `LivingEntity.addEffect`/`removeEffect`,
   or build `EntityEffectPacket`/`RemoveEntityEffectPacket` directly.
9. `WorldBorder.inBounds(Entity)` removed → `inBounds(entity.getPosition())`.
10. `AttackRange.effectiveMinReach(Entity)`/`effectiveMaxReach(Entity)` →
    `(GameMode gameMode, boolean isPlayer)`.
11. `BlockIterator(Entity)` / `BlockIterator(Entity, int)` constructors removed →
    `new BlockIterator(entity.getPosition(), eyeHeight, maxDistance)`.
12. `RelativeVec.from(Entity)`, `.fromView(Entity)`, `.fromSender(CommandSender)` → new
    framework class `net.minestom.server.utils.location.RelativeVecUtils`.
13. `SignatureValidator.from(Player)` removed; lib-safe `from(PlayerPublicKey)` added →
    `SignatureValidator.from(player.getPlayerConnection().playerPublicKey())`.
14. `Conditions.playerOnly` / `consoleOnly` → new framework class
    `net.minestom.server.command.builder.condition.SenderConditions` (same package);
    `Conditions.all/any/not` stay in lib.
15. `ping.Status.PlayerInfo.onlineCount()` / `online(int)` → statics on
    `event.server.ServerListPingEvent`. `Status.builder()` no longer defaults
    `playerInfo` to live online sampling (the default ping path still does).
16. `ServerListPingType.OPEN_TO_LAN.getPingResponse(Status)` now throws; use the pure
    `getOpenToLANPing(Status, int port)` (the framework's `OpenToLAN` supplies its port).
17. `AdventurePacketConvertor.createSoundPacket(Sound, Entity)` and
    `createEntitySoundPacket(Sound, Entity)` → `utils.PacketSendingUtils` (framework);
    a lib `createSoundPacket(Sound, int entityId)` overload was added.
18. `MetadataHolder(@Nullable Entity)` deprecated constructor removed → use the
    `Consumer`-based constructor.
19. Legacy `command.builder.parser.CommandParser.findCommand(CommandDispatcher, String)`
    → `findCommand(Function<String, Command>, String)` (no in-tree callers existed).

## Behavioral notes (signatures unchanged)

- `AsyncUtils.runAsync` completes its future exceptionally instead of routing to
  `ExceptionManager` (which previously completed it normally after swallowing).
- `MojangCrypt`, `RegistryData`, `SignatureValidator` log via local SLF4J loggers instead
  of `MinecraftServer.getExceptionManager()`/`LOGGER`.
- Lib serialization (`tag.Serializers`, NBT component serializer, deprecated `ItemStack`
  NBT overloads, adventure SPI) resolves registries via `Registries.staticRegistries()` —
  installed by the framework at boot, defaulting to `Registries.vanilla()` in lib-only use.

## Explicit non-breaks

`ServerFlag` and `MinecraftConstants` moved to `:lib` with packages unchanged;
`MinecraftServer` still `implements MinecraftConstants`, so `MinecraftServer.PROTOCOL_VERSION`
and friends resolve as before (`MinecraftConstants` is no longer `sealed`). The registry
freeze/tag-invalidation/compression holders, `GraphConverter` rework (package-private),
`Light.getNeighbors` relocation, collision-core extraction (`CollisionUtils`/`BlockCollision`
delegate, signatures intact), and all javadoc `{@link}` demotions change no public API.
`demo` and `testing` keep `requires net.minestom.server` against the aggregate. The
`minestom` Maven coordinate, its module name, and its jar contents are preserved.
