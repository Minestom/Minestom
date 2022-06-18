package net.minestom.server.message.registry;

import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public record ChatDecoration(@NotNull String translationKey, @NotNull List<Parameter> params, @NotNull Style style) implements NBTCompoundWriteable {
    public static final List<Parameter> PARAM_ALL = List.of(Parameter.TEAM_NAME, Parameter.SENDER, Parameter.CONTENT);
    public static final List<Parameter> PARAM_NAME = List.of(Parameter.SENDER, Parameter.CONTENT);

    @Override
    public void write(MutableNBTCompound compound) {
        compound.setString("translation_key", translationKey);
        compound.set("parameters", NBT.List(NBTType.TAG_String, params.stream().map(x -> NBT.String(x.name()
                .toLowerCase(Locale.ROOT))).collect(Collectors.toList())));
        compound.set("style", NBT.Compound(Map.of()));//TODO
    }

    public enum Parameter {
        SENDER, TEAM_NAME, CONTENT
    }
}
