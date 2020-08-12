package net.minestom.server.world.biomes;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class BiomeParticles {

	private final float probability;
	private final ParticleOptions options;

	public NBTCompound toNbt() {
		NBTCompound nbt = new NBTCompound();
		nbt.setFloat("probability", probability);
		nbt.set("options", options.toNbt());
		return nbt;
	}

	public interface ParticleOptions {
		NBTCompound toNbt();
	}

	@Getter
	@Builder
	@ToString
	@EqualsAndHashCode
	public static class BlockParticle implements ParticleOptions {

		//TODO also can be falling_dust
		private static final String type = "block";
		private final BlockAlternative block;

		@Override
		public NBTCompound toNbt() {
			NBTCompound nbtCompound = new NBTCompound();
			Block block1 = Block.fromStateId(block.getId());
			nbtCompound.setString("type", type);
			nbtCompound.setString("Name", block1.getName());
			Map<String, String> propertiesMap = block.createPropertiesMap();
			if (propertiesMap.size() != 0) {
				NBTCompound properties = new NBTCompound();
				propertiesMap.forEach(properties::setString);
				nbtCompound.set("Properties", properties);
			}
			return nbtCompound;
		}

	}

	@Getter
	@Builder
	@ToString
	@EqualsAndHashCode
	public static class DustParticle implements ParticleOptions {

		private static final String type = "dust";
		private final float red;
		private final float green;
		private final float blue;
		private final float scale;

		@Override
		public NBTCompound toNbt() {
			NBTCompound nbtCompound = new NBTCompound();
			nbtCompound.setString("type", type);
			nbtCompound.setFloat("r", red);
			nbtCompound.setFloat("g", green);
			nbtCompound.setFloat("b", blue);
			nbtCompound.setFloat("scale", scale);
			return nbtCompound;
		}

	}

	@Getter
	@Builder
	@ToString
	@EqualsAndHashCode
	public static class ItemParticle implements ParticleOptions {

		private static final String type = "item";
		private final ItemStack item;

		@Override
		public NBTCompound toNbt() {
			//todo test count might be wrong type
			NBTCompound nbtCompound = item.toNBT();
			nbtCompound.setString("type", type);
			return nbtCompound;
		}

	}

	@Getter
	@Builder
	@ToString
	@EqualsAndHashCode
	public static class NormalParticle implements ParticleOptions {

		private final NamespaceID type;

		@Override
		public NBTCompound toNbt() {
			NBTCompound nbtCompound = new NBTCompound();
			nbtCompound.setString("type", type.toString());
			return nbtCompound;
		}

	}
}
