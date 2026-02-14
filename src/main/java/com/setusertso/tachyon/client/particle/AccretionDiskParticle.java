package com.setusertso.tachyon.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class AccretionDiskParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected AccretionDiskParticle(ClientLevel level, double x, double y, double z,
                                     double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.lifetime = 60;
        this.quadSize = 0.15f;
        this.hasPhysics = false;
        // Start fully transparent
        this.alpha = 0.0f;
        // White color
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Move with NO friction/drag — constant velocity
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;

        // Fade in over first 15 ticks, then fade out over last 10
        float life = (float) this.age / (float) this.lifetime;
        if (life < 0.25f) {
            this.alpha = life / 0.25f; // fade in
        } else if (life > 0.83f) {
            this.alpha = (1.0f - life) / 0.17f; // fade out
        } else {
            this.alpha = 1.0f;
        }

        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                        double x, double y, double z,
                                        double vx, double vy, double vz) {
            return new AccretionDiskParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
