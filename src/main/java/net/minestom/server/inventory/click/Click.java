package net.minestom.server.inventory.click;

import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Click {

    /**
     * A tagged union representing possible clicks from the client.
     *
     * For a given inventory, any slot such that {@code slot >= inventory.getSize()} is
     * a slot within the inventory of the player who opened it. Converting this back
     * is as simple as subtracting the size of the inventory.
     *
     * For example, if a hopper is open, the slot {@code 5} indicates slot 0 in the
     * player's inventory, i.e. their first hotbar slot.
     */
    public sealed interface Info {

        record Left(int slot) implements Info {
        }

        record Right(int slot) implements Info {
        }

        record Middle(int slot) implements Info {
            // Creative only
        }

        record LeftShift(int slot) implements Info {
        }

        record RightShift(int slot) implements Info {
        }

        record Double(int slot) implements Info {
        }

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

        record LeftDropCursor() implements Info {
        }

        record RightDropCursor() implements Info {
        }

        record MiddleDropCursor() implements Info {
        }

        record DropSlot(int slot, boolean all) implements Info {
        }

        record HotbarSwap(int hotbarSlot, int clickedSlot) implements Info {
        }

        record OffhandSwap(int slot) implements Info {
        }

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
            final byte button = packet.button();
            final boolean requireCreative = switch (packet.clickType()) {
                case CLONE -> packet.slot() != -999; // Permit middle click dropping
                case QUICK_CRAFT -> button == 8 || button == 9 || button == 10;
                default -> false;
            };
            if (requireCreative && !isCreative) return null;

            final int slot = mapSlot(packet.slot(), containerSize);
            final int maxSize = containerSize != null ? containerSize + PlayerInventory.INNER_INVENTORY_SIZE : PlayerInventory.INVENTORY_SIZE;
            final boolean valid = slot >= 0 && slot < maxSize;

            if (valid) {
                return process(packet.clickType(), slot, button);
            } else {
                return slot == -999 ? processInvalidSlot(packet.clickType(), button) : null;
            }
        }

        private static int mapSlot(int slot, @Nullable Integer containerSize) {
            if (containerSize == null) {
                // No container means it's a player inventory slot, so convert it
                return PlayerInventoryUtils.convertWindow0SlotToMinestomSlot(slot);
            } else if (slot >= containerSize) {
                // If it's a player inventory slot above the container size, convert it to a Minestom player inventory slot.
                return containerSize + PlayerInventoryUtils.convertWindowSlotToMinestomSlot(slot, containerSize);
            } else {
                // Otherwise, no changes,
                return slot;
            }
        }

        private @Nullable Click.Info processInvalidSlot(@NotNull ClientClickWindowPacket.ClickType type, byte button) {
            return switch (type) {
                case PICKUP -> {
                    if (button == 0) yield new Info.LeftDropCursor();
                    if (button == 1) yield new Info.RightDropCursor();
                    yield null;
                }
                case CLONE -> {
                    if (button == 2) yield new Info.MiddleDropCursor(); // Why Mojang, why?
                    yield null;
                }
                case QUICK_CRAFT -> {
                    // Trust me, a switch would not make this cleaner

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

                    if (button == 0) leftDrag.clear();
                    if (button == 4) rightDrag.clear();
                    if (button == 8) middleDrag.clear();

                    yield null;
                }
                default -> null;
            };
        }

        /**
         * Processes a packet into click info.
         *
         * @param type          the type of the click
         * @param slot          the clicked slot
         * @param button        the sent button
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
                    switch (button) {
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

}
