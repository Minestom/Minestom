package net.minestom.server.item.metadata;

import net.minestom.server.chat.ChatColor;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.ArrayList;
import java.util.List;

public class MapMeta implements ItemMeta {

    private int mapId;
    private int mapScaleDirection = 1;
    private List<MapDecoration> decorations = new ArrayList<>();
    private ChatColor mapColor = ChatColor.NO_COLOR;

    public MapMeta() {}

    public MapMeta(int id) {
        this.mapId = id;
    }

    /**
     * Get the map id
     *
     * @return the map id
     */
    public int getMapId() {
        return mapId;
    }

    /**
     * Change the map id
     *
     * @param mapId the new map id
     */
    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    /**
     * Get the map scale direction
     *
     * @return the map scale direction
     */
    public int getMapScaleDirection() {
        return mapScaleDirection;
    }

    /**
     * Change the map scale direction
     *
     * @param mapScaleDirection the new map scale direction
     */
    public void setMapScaleDirection(int mapScaleDirection) {
        this.mapScaleDirection = mapScaleDirection;
    }

    /**
     * Get the map decorations
     *
     * @return a modifiable list containing all the map decorations
     */
    public List<MapDecoration> getDecorations() {
        return decorations;
    }

    /**
     * Change the map decorations list
     *
     * @param decorations the new map decorations list
     */
    public void setDecorations(List<MapDecoration> decorations) {
        this.decorations = decorations;
    }

    /**
     * Get the map color
     *
     * @return the map color
     */
    public ChatColor getMapColor() {
        return mapColor;
    }

    /**
     * Change the map color
     * <p>
     * WARNING: RGB colors are not supported
     *
     * @param mapColor the new map color
     */
    public void setMapColor(ChatColor mapColor) {
        mapColor.getId(); // used to throw an error if rgb color is used
        this.mapColor = mapColor;
    }

    @Override
    public boolean hasNbt() {
        return true;
    }

    @Override
    public boolean isSimilar(ItemMeta itemMeta) {
        if (!(itemMeta instanceof MapMeta))
            return false;

        final MapMeta mapMeta = (MapMeta) itemMeta;
        return mapMeta.mapId == mapId &&
                mapMeta.mapScaleDirection == mapScaleDirection &&
                mapMeta.decorations.equals(decorations) &&
                mapMeta.mapColor == mapColor;
    }

    @Override
    public void read(NBTCompound compound) {
        if (compound.containsKey("map")) {
            this.mapId = compound.getInt("map");
        }

        if (compound.containsKey("map_scale_direction")) {
            this.mapScaleDirection = compound.getInt("map_scale_direction");
        }

        if (compound.containsKey("Decorations")) {
            final NBTList<NBTCompound> decorationsList = compound.getList("Decorations");
            for (NBTCompound decorationCompound : decorationsList) {
                final String id = decorationCompound.getString("id");
                final byte type = decorationCompound.getByte("type");
                final byte x = decorationCompound.getByte("x");
                final byte z = decorationCompound.getByte("z");
                final double rotation = decorationCompound.getByte("rot");

                this.decorations.add(new MapDecoration(id, type, x, z, rotation));

            }
        }

        if (compound.containsKey("display")) {
            final NBTCompound displayCompound = compound.getCompound("display");
            if (displayCompound.containsKey("MapColor")) {
                final int color = displayCompound.getInt("MapColor");
                this.mapColor = ChatColor.fromId(color);
            }
        }

    }

    @Override
    public void write(NBTCompound compound) {
        compound.setInt("map", mapId);

        compound.setInt("map_scale_direction", mapScaleDirection);

        if (!decorations.isEmpty()) {
            NBTList<NBTCompound> decorationsList = new NBTList<>(NBTTypes.TAG_Compound);
            for (MapDecoration decoration : decorations) {
                NBTCompound decorationCompound = new NBTCompound();
                decorationCompound.setString("id", decoration.getId());
                decorationCompound.setByte("type", decoration.getType());
                decorationCompound.setByte("x", decoration.getX());
                decorationCompound.setByte("z", decoration.getZ());
                decorationCompound.setDouble("rot", decoration.getRotation());

                decorationsList.add(decorationCompound);
            }
            compound.set("Decorations", decorationsList);
        }

        {
            NBTCompound displayCompound;
            if (compound.containsKey("display")) {
                displayCompound = compound.getCompound("display");
            } else {
                displayCompound = new NBTCompound();
            }
            displayCompound.setInt("MapColor", mapColor.getId());
        }
    }

    @Override
    public ItemMeta clone() {
        MapMeta mapMeta = new MapMeta();
        mapMeta.setMapId(mapId);
        mapMeta.setMapScaleDirection(mapScaleDirection);
        mapMeta.decorations.addAll(decorations);
        mapMeta.setMapColor(mapColor);
        return mapMeta;
    }

    public static class MapDecoration {
        private String id;
        private byte type;
        private byte x, z;
        private double rotation;

        public MapDecoration(String id, byte type, byte x, byte z, double rotation) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.z = z;
            this.rotation = rotation;
        }

        /**
         * Get the arbitrary decoration id
         *
         * @return the decoration id
         */
        public String getId() {
            return id;
        }

        /**
         * Get the decoration type
         *
         * @return the decoration type
         * @see <a href="https://minecraft.gamepedia.com/Map#Map_icons">Map icons</a>
         */
        public byte getType() {
            return type;
        }

        /**
         * Get the X position of the decoration
         *
         * @return the X position
         */
        public byte getX() {
            return x;
        }

        /**
         * Get the Z position of the decoration
         *
         * @return the Z position
         */
        public byte getZ() {
            return z;
        }

        /**
         * Get the rotation of the symbol (0;360)
         *
         * @return the rotation of the symbol
         */
        public double getRotation() {
            return rotation;
        }
    }

}
