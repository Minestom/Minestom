package net.minestom.server.instance.generator;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.generator.units.GenerationRequest;
import net.minestom.server.instance.generator.units.GenerationResponse;

public interface Generator<T extends GenerationRequest<R>, R extends GenerationResponse<?>> {
    R generate(Instance instance, T request);
    Class<T> supportedRequestType();
}
