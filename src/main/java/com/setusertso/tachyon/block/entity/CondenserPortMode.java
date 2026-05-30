package com.setusertso.tachyon.block.entity;

import net.minecraft.util.StringRepresentable;

public enum CondenserPortMode implements StringRepresentable {
    ITEM_INPUT("item_input"),
    FLUID_OUTPUT("fluid_output"),
    ENERGY_INPUT("energy_input");

    private final String serializedName;

    CondenserPortMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public CondenserPortMode next() {
        CondenserPortMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static CondenserPortMode fromOrdinal(int ordinal) {
        CondenserPortMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ITEM_INPUT;
    }
}
