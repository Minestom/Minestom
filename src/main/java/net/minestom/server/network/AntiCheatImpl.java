package net.minestom.server.network;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientKeepAlivePacket;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.network.packet.client.common.ClientSettingsPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import net.minestom.server.network.packet.server.play.*;

import java.util.ArrayList;
import java.util.List;

public class AntiCheatImpl implements AntiCheat {
    private static final Action VALID = new Action.Valid();
    private boolean joined = false;

    private int estimatedLatency = 300;
    private long lastKeepAlive = -1;

    private Player player = new Player();
    private final List<Change> changes = new ArrayList<>();

    @Override
    public void consume(ServerPacket.Play serverPacket) {
        switch (serverPacket) {
            case JoinGamePacket joinGamePacket -> {
                this.joined = true;
                addChange(new Change.SetEntityId(joinGamePacket.entityId()));
                addChange(new Change.SetGameMode(joinGamePacket.gameMode()));
            }
            case KeepAlivePacket keepAlivePacket -> {
                this.lastKeepAlive = System.currentTimeMillis();
            }
            case RespawnPacket respawnPacket -> {
                addChange(new Change.SetGameMode(respawnPacket.gameMode()));
            }
            case ChangeGameStatePacket changeGameStatePacket -> {
                switch (changeGameStatePacket.reason()) {
                    case CHANGE_GAMEMODE -> {
                        addChange(new Change.SetGameMode(GameMode.fromId((int) changeGameStatePacket.value())));
                    }
                    default -> {
                        // dont throw exception here tf
                        //throw new IllegalStateException("Unexpected value: " + changeGameStatePacket);
                    }
                }
            }
            case OpenWindowPacket openWindowPacket -> {
                addChange(new Change.SetInInventory(true));
            }
            case CloseWindowPacket closeWindowPacket -> {
                addChange(new Change.SetInInventory(false));
            }
            default -> {
                // Empty
            }
        }
    }

    @Override
    public Action consume(ClientPacket clientPacket) {
        if (!joined) return new Action.InvalidCritical("Client packet received before join game packet.");

        return switch (clientPacket) {
            case ClientAnimationPacket packet -> {
                addChange(new Change.SetArmSwing(true));
                yield VALID;
            }
            case ClientNameItemPacket packet -> {
                if (packet.itemName().length() > 50) {
                    yield new Action.InvalidCritical("Item name too long.");
                }
                yield VALID;
            }
            case ClientSettingsPacket packet -> {
                if (packet.viewDistance() < 2 || packet.viewDistance() > 32) {
                    yield new Action.InvalidIgnore("Invalid view distance.");
                }
                String locale = packet.locale();
                int localeLength = locale.length();
                if (localeLength < 4 || localeLength > 6) {
                    yield new Action.InvalidIgnore("Locale setting received is too short or too big.");
                }
                addChange(new Change.SetSettings(new Settings(packet.chatMessageType(), packet.viewDistance())));
                yield VALID;
            }
            case ClientEntityActionPacket packet -> {
                if (player.inInventory) {
                    yield new Action.InvalidCritical("Entity action packet received while in window.");
                }

                var action = packet.action();
                var jumpBoost = packet.horseJumpBoost();
                if (jumpBoost < 0 || jumpBoost > 100) {
                    yield new Action.InvalidCritical("Invalid horse jump boost.");
                }
                if (jumpBoost != 0 && action != ClientEntityActionPacket.Action.START_JUMP_HORSE) {
                    yield new Action.InvalidCritical("Horse jump boost without starting jump.");
                }
                yield VALID;
            }
            case ClientSpectatePacket packet -> {
                if (gameMode() != GameMode.SPECTATOR) {
                    yield new Action.InvalidCritical("Spectate packet received in non-spectator game mode.");
                }
                yield VALID;
            }
            case ClientInteractEntityPacket packet -> {
                if (player.inInventory) {
                    yield new Action.InvalidCritical("Interact entity packet received while in window.");
                }

                if (packet.type() instanceof ClientInteractEntityPacket.Attack) {
                    if (!player.swungHand) {
                        yield new Action.InvalidCritical("Attack packet received while already attacking.");
                    }
                    addChange(new Change.SetArmSwing(false));
                }
                if (packet.targetId() == player.entityId) { // Impossible to interact with self.
                    yield new Action.InvalidCritical("Interact entity packet received with self as target.");
                }
                yield VALID;
            }
            case ClientHeldItemChangePacket packet -> {
                if (packet.slot() < 0 || packet.slot() > 8) {
                    yield new Action.InvalidCritical("Invalid held item slot.");
                }
                if (packet.slot() == player.lastHeldSlot) {
                    yield new Action.InvalidIgnore("Held item change packet received with the same slot.");
                }
                addChange(new Change.SetLastHeldSlot(packet.slot()));
                yield VALID;
            }
            // Very basic yaw pitch verification.
            case ClientPlayerPositionAndRotationPacket packet -> verifyRotationState(packet.position().pitch(), packet.position().yaw());
            case ClientPlayerRotationPacket packet -> verifyRotationState(packet.pitch(), packet.yaw());

            case ClientChatMessagePacket packet -> {
                if (player.inInventory) {
                    yield new Action.InvalidCritical("Chat message packet received while in window.");
                }

                // The vanilla client cannot send a chat message if they do not have chat visible, or if they are sneaking or sprinting.
                if (settings() == null)
                    yield new Action.InvalidCritical("Chat message packet received before settings packet.");
                if (settings().chatMessageType  == ChatMessageType.NONE)
                    yield new Action.InvalidIgnore("Chat message packet received with chat visibility set to NONE.");
                if (sneaking() || sprinting()) {
                    yield new Action.InvalidCritical("Chat message packet received while sneaking or sprinting.");
                }
                yield VALID;
            }
            case ClientCreativeInventoryActionPacket packet -> {
                if (gameMode() != GameMode.CREATIVE)
                    yield new Action.InvalidCritical("Creative inventory action packet received in non-creative game mode.");
                yield VALID;
            }
            case ClientPlayerBlockPlacementPacket packet -> {
                if (packet.cursorPositionX() > 1.0 || packet.cursorPositionY() > 1.0 || packet.cursorPositionZ() > 1.0)
                    yield new Action.InvalidCritical("Invalid block cursor position.");
                if (packet.cursorPositionX() < 0.0 || packet.cursorPositionY() < 0.0 || packet.cursorPositionZ() < 0.0)
                    yield new Action.InvalidCritical("Invalid block cursor position.");

                yield VALID;
            }
            case ClientPlayerPositionPacket packet -> {
                if (player.position == null) {
                    addChange(new Change.SetPosition(packet.position()));
                }

                if (player.inInventory && !player.position.samePoint(packet.position()))
                    yield new Action.InvalidCritical("Position packet received while in window.");

                addChange(new Change.SetPosition(packet.position()));
                yield VALID;
            }
            case ClientVehicleMovePacket packet -> {
                // TODO: do the update to set this boolean
                if (!player.inVehicle) {
                    yield new Action.InvalidIgnore("Vehicle move packet received while not in vehicle.");
                }

                yield VALID;
            }
            case ClientCloseWindowPacket packet -> {
                addChange(new Change.SetInInventory(false));

                yield VALID;
            }
            case ClientCommandChatPacket packet -> {
                if (player.inInventory) {
                    yield new Action.InvalidIgnore("Command packet received while in window.");
                }

                yield VALID;
            }
            case ClientKeepAlivePacket packet -> {
                final long lastKeepAlive = this.lastKeepAlive;
                if (lastKeepAlive == -1) yield new Action.InvalidIgnore("Stray keep alive packet received.");
                this.estimatedLatency = (int) (System.currentTimeMillis() - lastKeepAlive);
                this.lastKeepAlive = -1;
                yield VALID;
            }
            default -> VALID;
        };
    }

    private Action verifyRotationState(float pitch, float yaw) {
        if (pitch > 90 || pitch < -90) {
            return new Action.InvalidCritical("Pitch out of bounds.");
        }
        if (Math.abs(pitch) > 90) {
            return new Action.InvalidCritical("Pitch out of bounds.");
        }
        if (!Float.isFinite(pitch) || !Float.isFinite(yaw)) {
            return new Action.InvalidCritical("Pitch or yaw is not finite.");
        }
        return VALID;
    }

    private void addChange(Change change) {
        this.changes.add(change);
        // TODO: delay
        this.player = this.player.merge(this.changes);
        this.changes.clear();
    }

    Settings settings() {
        return player.settings;
    }

    GameMode gameMode() {
        // TODO: account for latency, loop over changes if not empty
        return player.gameMode;
    }

    boolean sneaking() {
        return player.movementState.sneaking;
    }

    boolean sprinting() {
        return player.movementState.sprinting;
    }

    record Player(
            int entityId,
            Settings settings,
            GameMode gameMode,
            MovementState movementState,
            Point position,
            int lastHeldSlot,
            boolean swungHand,
            boolean inVehicle,
            boolean inInventory
    ) {

        public Player() {
            this(Integer.MIN_VALUE, null, null, null, null, 0, false, false, false);
        }

        Player merge(List<Change> changes) {
            GameMode gameMode = this.gameMode;
            Settings settings = this.settings;
            MovementState movementState = this.movementState;
            Point position = this.position;
            int lastHeldSlot = this.lastHeldSlot;
            int entityId = this.entityId;
            var swingsArm = this.swungHand;
            var inVehicle = this.inVehicle;
            var inInventory = this.inInventory;
            for (Change change : changes) {
                switch (change) {
                    case Change.SetGameMode setGameMode -> gameMode = setGameMode.gameMode;
                    case Change.SetSettings setSettings ->  settings =setSettings.settings();
                    case Change.SetMovementState setMovementState -> movementState = setMovementState.movementState();
                    case Change.SetPosition setPosition -> position = setPosition.position();
                    case Change.SetLastHeldSlot setLastHeldSlot -> lastHeldSlot = setLastHeldSlot.slot();
                    case Change.SetEntityId setEntityId -> entityId = setEntityId.entityId();
                    case Change.SetArmSwing setArmSwing -> swingsArm = setArmSwing.swung;
                    case Change.SetInVehicle setInVehicle -> inVehicle = setInVehicle.inVehicle;
                    case Change.SetInInventory setInInventory -> inInventory = setInInventory.inInventory;
                }
            }
            return new Player(entityId, settings, gameMode, movementState, position, lastHeldSlot, swingsArm, inVehicle, inInventory);
        }
    }


    record Settings(ChatMessageType chatMessageType, int viewDistance) {

    }
    record MovementState(boolean sneaking, boolean sprinting) {}

    sealed interface Change {
        record SetMovementState(MovementState movementState) implements Change {}
        record SetGameMode(GameMode gameMode) implements Change {}
        record SetSettings(Settings settings) implements Change {}
        record SetLastHeldSlot(int slot) implements Change {}
        record SetEntityId(int entityId) implements Change {}
        record SetArmSwing(boolean swung) implements Change {}
        record SetInVehicle(boolean inVehicle) implements Change {}
        record SetInInventory(boolean inInventory) implements Change {}
        record SetPosition(Point position) implements Change {}
    }
}
