package com.setusertso.tachyon.block.entity;

import net.minecraft.util.StringRepresentable;

public enum PortMode implements StringRepresentable {
    ITEM_INPUT("item_input"),
    ITEM_OUTPUT("item_output"),
    FLUID_INPUT("fluid_input"),
    FLUID_OUTPUT("fluid_output"),
    ENERGY_INPUT("energy_input"),
    ENERGY_OUTPUT("energy_output");

    private final String serializedName;

    PortMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public PortMode next() {
        PortMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static PortMode fromOrdinal(int ordinal) {
        PortMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ITEM_INPUT;
    }
}
