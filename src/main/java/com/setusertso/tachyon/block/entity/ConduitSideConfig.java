package com.setusertso.tachyon.block.entity;

import net.minecraft.util.StringRepresentable;

/**
 * Transfer mode for a single resource type on a single side.
 * PUSH = conduit pushes resources out this side into neighbors
 * PULL = conduit pulls resources in from neighbors on this side
 * BOTH = push and pull
 * DISABLED = no transfer on this side for this resource type
 */
public enum ConduitSideConfig implements StringRepresentable {
    PUSH("push"),
    PULL("pull"),
    BOTH("both"),
    DISABLED("disabled");

    private final String serializedName;

    ConduitSideConfig(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public ConduitSideConfig next() {
        ConduitSideConfig[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static ConduitSideConfig fromOrdinal(int ordinal) {
        ConduitSideConfig[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : BOTH;
    }

    public boolean canPull() {
        return this == PULL || this == BOTH;
    }

    public boolean canPush() {
        return this == PUSH || this == BOTH;
    }

    public String displayName() {
        return switch (this) {
            case PUSH -> "Push";
            case PULL -> "Pull";
            case BOTH -> "Both";
            case DISABLED -> "Disabled";
        };
    }
}
