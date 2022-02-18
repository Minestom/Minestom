package net.minestom.server.item.metadata;

import net.minestom.server.color.Color;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapMeta extends ItemMeta implements ItemMetaBuilder.Provider<MapMeta.Builder> {

    private final int mapId;
    private final int mapScaleDirection;
    private final List<Decoration> decorations;
    private final Color mapColor;

    protected MapMeta(ItemMetaBuilder metaBuilder,
                      int mapId,
                      int mapScaleDirection,
                      @NotNull List<Decoration> decorations,
                      @NotNull Color mapColor) {
        super(metaBuilder);
        this.mapId = mapId;
        this.mapScaleDirection = mapScaleDirection;
        this.decorations = List.copyOf(decorations);
        this.mapColor = mapColor;
    }

    /**
     * Gets the map id.
     *
     * @return the map id
     */
    public int getMapId() {
        return mapId;
    }

    /**
     * Gets the map scale direction.
     *
     * @return the map scale direction
     */
    public int getMapScaleDirection() {
        return mapScaleDirection;
    }

    /**
     * Gets the map decorations.
     *
     * @return a modifiable list containing all the map decorations
     */
    public List<Decoration> getDecorations() {
        return decorations;
    }

    /**
     * Gets the map color.
     *
     * @return the map color
     */
    public @NotNull Color getMapColor() {
        return this.mapColor;
    }

    public static class Builder extends ItemMetaBuilder {

        private int mapId;
        private int mapScaleDirection = 1;
        private List<Decoration> decorations = new CopyOnWriteArrayList<>();
        private Color mapColor = new Color(0, 0, 0);

        public Builder mapId(int value) {
            this.mapId = value;
            mutableNbt().setInt("map", mapId);
            return this;
        }

        public Builder mapScaleDirection(int value) {
            this.mapScaleDirection = value;
            mutableNbt().setInt("map_scale_direction", value);
            return this;
        }

        public Builder decorations(List<Decoration> value) {
            this.decorations = new ArrayList<>(value);
            mutableNbt().set("Decorations", NBT.List(NBTType.TAG_Compound,
                    decorations.stream()
                            .map(decoration -> NBT.Compound(Map.of(
                                    "id", NBT.String(decoration.id()),
                                    "type", NBT.Byte(decoration.type()),
                                    "x", NBT.Byte(decoration.x()),
                                    "z", NBT.Byte(decoration.z()),
                                    "rot", NBT.Double(decoration.rotation()))))
                            .toList()
            ));
            return this;
        }

        public Builder mapColor(Color value) {
            this.mapColor = value;
            handleCompound("display", displayCompound -> displayCompound.setInt("MapColor", mapColor.asRGB()));
            return this;
        }

        @Override
        public @NotNull ItemMeta build() {
            return new MapMeta(this, mapId, mapScaleDirection, decorations, mapColor);
        }

        @Override
        public void read(@NotNull NBTCompound compound) {
            if (compound.get("map") instanceof NBTInt mapInt) {
                this.mapId = mapInt.getValue();
            }
            if (compound.get("map_scale_direction") instanceof NBTInt mapScaleDirection) {
                this.mapScaleDirection = mapScaleDirection.getValue();
            }

            if (compound.get("Decorations") instanceof NBTList<?> decorationsList &&
                    decorationsList.getSubtagType() == NBTType.TAG_Compound) {
                List<Decoration> decorations = new ArrayList<>();
                for (NBTCompound decorationCompound : decorationsList.<NBTCompound>asListOf()) {
                    final String id = decorationCompound.getString("id");
                    final byte type = decorationCompound.getAsByte("type");
                    byte x = 0;

                    if (decorationCompound.get("x") instanceof NBTByte xByte) {
                        x = xByte.getValue();
                    }

                    byte z = 0;
                    if (decorationCompound.get("z") instanceof NBTByte zByte) {
                        z = zByte.getValue();
                    }

                    double rotation = 0.0;
                    if (decorationCompound.get("rot") instanceof NBTDouble rotDouble) {
                        rotation = rotDouble.getValue();
                    }

                    decorations.add(new Decoration(id, type, x, z, rotation));
                }
                this.decorations = decorations;
            }

            if (compound.get("display") instanceof NBTCompound displayCompound) {
                if (displayCompound.get("MapColor") instanceof NBTInt mapColor) {
                    this.mapColor = new Color(mapColor.getValue());
                }
            }
        }
    }

    public record Decoration(String id, byte type, byte x, byte z, double rotation) {
    }
}
