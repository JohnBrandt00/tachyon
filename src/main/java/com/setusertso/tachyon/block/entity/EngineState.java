package com.setusertso.tachyon.block.entity;

public enum EngineState {
    NORMAL,
    MELTDOWN,
    NEUTRALIZED;

    public static EngineState fromOrdinal(int ordinal) {
        EngineState[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NORMAL;
    }
}
