package net.minestom.server.attribute;

import java.util.UUID;

import io.netty.util.internal.ThreadLocalRandom;
import net.minestom.server.utils.UniqueIdUtils;
import org.jetbrains.annotations.NotNull;

public class AttributeModifier {

	private final float amount;
	private final String name;
	private final AttributeOperation operation;
	private final UUID id;

	public AttributeModifier(@NotNull String name, float amount, @NotNull AttributeOperation operation) {
		this(UniqueIdUtils.createRandomUUID(ThreadLocalRandom.current()), name, amount, operation);
	}

	public AttributeModifier(@NotNull UUID id, @NotNull String name, float amount, @NotNull AttributeOperation operation) {
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.operation = operation;
	}

	@NotNull
	public UUID getId() {
		return id;
	}

	@NotNull
	public String getName() {
		return name;
	}

	public float getAmount() {
		return amount;
	}

	@NotNull
	public AttributeOperation getOperation() {
		return operation;
	}
}
