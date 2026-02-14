package com.setusertso.tachyon.init;

import com.setusertso.tachyon.tachyon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, tachyon.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BLACK_HOLE_AMBIENT = SOUND_EVENTS.register(
        "black_hole_ambient",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "black_hole_ambient"))
    );
}
