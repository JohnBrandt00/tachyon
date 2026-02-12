package com.setusertso.tachyon.block.entity;

public enum PortMode {
    ITEM_INPUT("item_input"),
    ITEM_OUTPUT("item_output"),
    FLUID_INPUT("fluid_input"),
    FLUID_OUTPUT("fluid_output"),
    ENERGY_INPUT("energy_input");

    private final String serializedName;

    PortMode(String serializedName) {
        this.serializedName = serializedName;
    }

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
