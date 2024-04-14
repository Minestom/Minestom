package net.minestom.server.inventory.click;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

public final class Click {

    /**
     * Contains information about a click. These are equal to the packet slot IDs from <a href="https://wiki.vg/Inventory">the Minecraft protocol.</a>.
     * The inventory used should be known from context.
     */
    public sealed interface Info {
        record Left(int slot) implements Info {}
        record Right(int slot) implements Info {}
        record Middle(int slot) implements Info {
            // Creative only
        }

        record LeftShift(int slot) implements Info {}
        record RightShift(int slot) implements Info {}

        record Double(int slot) implements Info {}

        record LeftDrag(List<Integer> slots) implements Info {
            public LeftDrag {
                slots = List.copyOf(slots);
            }
        }

        record RightDrag(List<Integer> slots) implements Info {
            public RightDrag {
                slots = List.copyOf(slots);
            }
        }

        record MiddleDrag(List<Integer> slots) implements Info {
            // Creative only
            public MiddleDrag {
                slots = List.copyOf(slots);
            }
        }

        record LeftDropCursor() implements Info {}
        record RightDropCursor() implements Info {}
        record MiddleDropCursor() implements Info {}

        record DropSlot(int slot, boolean all) implements Info {}

        record HotbarSwap(int hotbarSlot, int clickedSlot) implements Info {}
        record OffhandSwap(int slot) implements Info {}

        record CreativeSetItem(int slot, @NotNull ItemStack item) implements Info {}
        record CreativeDropItem(@NotNull ItemStack item) implements Info {}
    }

    /**
     * Preprocesses click packets, turning them into {@link Info} instances for further processing.
     */
    public static final class Preprocessor {
        private final Set<Integer> leftDrag = new LinkedHashSet<>();
        private final Set<Integer> rightDrag = new LinkedHashSet<>();
        private final Set<Integer> middleDrag = new LinkedHashSet<>();

        public void clearCache() {
            this.leftDrag.clear();
            this.rightDrag.clear();
            this.middleDrag.clear();
        }

        /**
         * Processes the provided click packet, turning it into a {@link Info}.
         * This will do simple verification of the packet before sending it to {@link #process(ClientClickWindowPacket.ClickType, int, byte)}.
         *
         * @param packet        the raw click packet
         * @param isCreative    whether the player is in creative mode (used for ignoring some actions)
         * @param containerSize the size of the open container, or null if the player inventory is open
         * @return the information about the click, or nothing if there was no immediately usable information
         */
        public @Nullable Click.Info processClick(@NotNull ClientClickWindowPacket packet, boolean isCreative, @Nullable Integer containerSize) {
            if (requireCreative(packet) && !isCreative) return null;
            final int slot = packet.slot() == -999 ? -999 :
                    containerSize == null ? PlayerInventoryUtils.protocolToMinestom(packet.slot()) : packet.slot();
            final int maxSize = containerSize != null ? containerSize + PlayerInventoryUtils.INNER_SIZE : PlayerInventoryUtils.INVENTORY_SIZE;
            if (packet.clickType() == ClientClickWindowPacket.ClickType.PICKUP && slot == -999) {
                if (packet.button() == 0) return new Info.LeftDropCursor();
                if (packet.button() == 1) return new Info.RightDropCursor();
                if (packet.button() == 2) return new Info.MiddleDropCursor();
            }
            final boolean valid = slot >= 0 && slot < maxSize;
            if (!valid) return null;
            return process(packet.clickType(), slot, packet.button());
        }

        private boolean requireCreative(ClientClickWindowPacket packet) {
            final byte button = packet.button();
            return switch (packet.clickType()) {
                case CLONE -> true;
                case QUICK_CRAFT -> button == 8 || button == 9 || button == 10;
                default -> false;
            };
        }

        /**
         * Processes a packet into click info.
         *
         * @param type   the type of the click
         * @param slot   the clicked slot
         * @param button the sent button
         * @return the information about the click, or nothing if there was no immediately usable information
         */
        private @Nullable Click.Info process(@NotNull ClientClickWindowPacket.ClickType type, int slot, byte button) {
            return switch (type) {
                case PICKUP -> switch (button) {
                    case 0 -> new Info.Left(slot);
                    case 1 -> new Info.Right(slot);
                    default -> null;
                };
                case QUICK_MOVE -> button == 0 ? new Info.LeftShift(slot) : new Info.RightShift(slot);
                case SWAP -> {
                    if (button >= 0 && button < 9) {
                        yield new Info.HotbarSwap(button, slot);
                    } else if (button == 40) {
                        yield new Info.OffhandSwap(slot);
                    } else {
                        yield null;
                    }
                }
                case CLONE -> new Info.Middle(slot);
                case THROW -> new Info.DropSlot(slot, button == 1);
                case QUICK_CRAFT -> {
                    // Handle drag finishes
                    if (button == 2) {
                        var list = List.copyOf(leftDrag);
                        leftDrag.clear();
                        yield new Info.LeftDrag(list);
                    } else if (button == 6) {
                        var list = List.copyOf(rightDrag);
                        rightDrag.clear();
                        yield new Info.RightDrag(list);
                    } else if (button == 10) {
                        var list = List.copyOf(middleDrag);
                        middleDrag.clear();
                        yield new Info.MiddleDrag(list);
                    }

                    switch (button) {
                        case 0 -> leftDrag.clear();
                        case 4 -> rightDrag.clear();
                        case 8 -> middleDrag.clear();

                        case 1 -> leftDrag.add(slot);
                        case 5 -> rightDrag.add(slot);
                        case 9 -> middleDrag.add(slot);
                    }
                    yield null;
                }
                case PICKUP_ALL -> new Info.Double(slot);
            };
        }
    }

    public record Getter(@NotNull IntFunction<ItemStack> main, @NotNull IntFunction<ItemStack> player,
                         @NotNull ItemStack cursor, int mainSize) {
        public @NotNull ItemStack get(int slot) {
            if (slot < mainSize()) {
                return main.apply(slot);
            } else {
                return player.apply(PlayerInventoryUtils.protocolToMinestom(slot, mainSize()));
            }
        }
    }

    public sealed interface Change {
        record Main(int slot, @NotNull ItemStack item) implements Change {
        }

        record Player(int slot, @NotNull ItemStack item) implements Change {
        }

        record Cursor(@NotNull ItemStack item) implements Change {
        }

        record DropFromPlayer(@NotNull ItemStack item) implements Change {
        }
    }
}
