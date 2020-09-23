package net.minestom.server.world.biomes;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class Biome {

	public static final AtomicInteger idCounter = new AtomicInteger(0);

	//A plains biome has to be registered or else minecraft will crash
	public static final Biome PLAINS = Biome.builder()
			.category(Category.NONE)
			.name(NamespaceID.from("minecraft:plains"))
			.temperature(0.8F)
			.downfall(0.4F)
			.depth(0.125F)
			.scale(0.05F)
			.effects(BiomeEffects.builder()
					.fog_color(0xC0D8FF)
					.sky_color(0x78A7FF)
					.water_color(0x3F76E4)
					.water_fog_color(0x50533)
					.build())
			.build();

	private final int id = idCounter.getAndIncrement();

	private final NamespaceID name;
	@Builder.Default
	private final float depth = 0.2F;
	@Builder.Default
	private final float temperature = 0.25F;
	@Builder.Default
	private final float scale = 0.2F;
	@Builder.Default
	private final float downfall = 0.8F;
	@Builder.Default
	private final Category category = Category.NONE;
	private final BiomeEffects effects;
	@Builder.Default
	private final Precipitation precipitation = Precipitation.RAIN;
	@Builder.Default
	private TemperatureModifier temperature_modifier = TemperatureModifier.NONE;

	public NBTCompound toNbt() {
		NBTCompound nbt = new NBTCompound();
		nbt.setString("name", name.toString());
		nbt.setInt("id", getId());

		NBTCompound element = new NBTCompound();
		element.setFloat("depth", depth);
		element.setFloat("temperature", temperature);
		element.setFloat("scale", scale);
		element.setFloat("downfall", downfall);
		element.setString("category", category.getType());
		element.setString("precipitation", precipitation.getType());
		if (temperature_modifier != TemperatureModifier.NONE)
			element.setString("temperature_modifier", temperature_modifier.getType());
		element.set("effects", effects.toNbt());
		nbt.set("element", element);
		return nbt;
	}

	public enum Precipitation {
		RAIN("rain"), NONE("none"), SNOW("snow");

		@Getter
		String type;

		Precipitation(String type) {
			this.type = type;
		}
	}

	public enum Category {
		NONE("none"), TAIGA("taiga"), EXTREME_HILLS("extreme_hills"), JUNGLE("jungle"), MESA("mesa"), PLAINS("plains"),
		SAVANNA("savanna"), ICY("icy"), THE_END("the_end"), BEACH("beach"), FOREST("forest"), OCEAN("ocean"),
		DESERT("desert"), RIVER("river"), SWAMP("swamp"), MUSHROOM("mushroom"), NETHER("nether");

		@Getter
		String type;

		Category(String type) {
			this.type = type;
		}
	}

	public enum TemperatureModifier {
		NONE("none"), FROZEN("frozen");

		@Getter
		String type;

		TemperatureModifier(String type) {
			this.type = type;
		}
	}

}
