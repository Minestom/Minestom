package net.minestom.server.world.biomes;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class BiomeEffects {

	private final int fog_color;
	private final int sky_color;
	private final int water_color;
	private final int water_fog_color;
	@Builder.Default private int foliage_color = -1;
	@Builder.Default private int grass_color = -1;
	@Builder.Default private GrassColorModifier grass_color_modifier = null;
	@Builder.Default private BiomeParticles biomeParticles = null;
	@Builder.Default private NamespaceID ambient_sound = null;
	@Builder.Default private MoodSound mood_sound = null;
	@Builder.Default private AdditionsSound additions_sound = null;
	@Builder.Default private Music music = null;

	public NBTCompound toNbt() {
		NBTCompound nbt = new NBTCompound();
		nbt.setInt("fog_color", fog_color);
		if (foliage_color != -1)
			nbt.setInt("foliage_color", foliage_color);
		if (grass_color != -1)
			nbt.setInt("grass_color", grass_color);
		nbt.setInt("sky_color", sky_color);
		nbt.setInt("water_color", water_color);
		nbt.setInt("water_fog_color", water_fog_color);
		if (grass_color_modifier != null)
			nbt.setString("grass_color_modifier", grass_color_modifier.getType());
		if (biomeParticles != null)
			nbt.set("particle", biomeParticles.toNbt());
		if (ambient_sound != null)
			nbt.setString("ambient_sound", ambient_sound.toString());
		if (mood_sound != null)
			nbt.set("mood_sound", mood_sound.toNbt());
		if (additions_sound != null)
			nbt.set("additions_sound", additions_sound.toNbt());
		if (music != null)
			nbt.set("music", music.toNbt());
		return nbt;
	}

	public enum GrassColorModifier {
		NONE("none"), DARK_FOREST("dark_forest"), SWAMP("swamp");

		@Getter
		String type;

		GrassColorModifier(String type) {
			this.type = type;
		}
	}

	@Getter
	@Builder
	@ToString
	@EqualsAndHashCode
	public static class MoodSound {

		private final NamespaceID sound;
		private final int tick_delay;
		private final int block_search_extent;
		private final double offset;

		public NBTCompound toNbt() {
			NBTCompound nbt = new NBTCompound();
			nbt.setString("sound", sound.toString());
			nbt.setInt("tick_delay", tick_delay);
			nbt.setInt("block_search_extent", block_search_extent);
			nbt.setDouble("offset", offset);
			return nbt;
		}

	}

	@Getter
	@Builder
	@ToString
	@EqualsAndHashCode
	public static class AdditionsSound {

		private final NamespaceID sound;
		private final double tick_chance;

		public NBTCompound toNbt() {
			NBTCompound nbt = new NBTCompound();
			nbt.setString("sound", sound.toString());
			nbt.setDouble("tick_chance", tick_chance);
			return nbt;
		}

	}

	@Getter
	@Builder
	@ToString
	@EqualsAndHashCode
	public static class Music {

		private final NamespaceID sound;
		private final int min_delay;
		private final int max_delay;
		private final boolean replace_current_music;

		public NBTCompound toNbt() {
			NBTCompound nbt = new NBTCompound();
			nbt.setString("sound", sound.toString());
			nbt.setInt("min_delay", min_delay);
			nbt.setInt("max_delay", max_delay);
			nbt.setByte("replace_current_music", replace_current_music ? (byte) 1 : (byte) 0);
			return nbt;
		}

	}

}
