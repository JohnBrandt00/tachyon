package com.setusertso.tachyon.init;

import com.setusertso.tachyon.tachyon;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, tachyon.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ACCRETION_DISK =
        PARTICLE_TYPES.register("accretion_disk", () -> new SimpleParticleType(false));
}
