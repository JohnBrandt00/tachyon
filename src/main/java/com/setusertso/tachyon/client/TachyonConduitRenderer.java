package com.setusertso.tachyon.client;

import com.setusertso.tachyon.block.TachyonConduitBlock;
import com.setusertso.tachyon.block.entity.ConduitSideConfig;
import com.setusertso.tachyon.block.entity.TachyonConduitBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.joml.Vector3f;

import static com.setusertso.tachyon.block.entity.TachyonConduitBlockEntity.*;

public class TachyonConduitRenderer implements BlockEntityRenderer<TachyonConduitBlockEntity> {

    // Yellow for energy, green for items, cyan for fluid
    private static final DustParticleOptions ENERGY_DUST =
            new DustParticleOptions(new Vector3f(1.0f, 0.9f, 0.2f), 0.5f);
    private static final DustParticleOptions ITEM_DUST =
            new DustParticleOptions(new Vector3f(0.3f, 1.0f, 0.3f), 0.5f);
    private static final DustParticleOptions FLUID_DUST =
            new DustParticleOptions(new Vector3f(0.3f, 0.7f, 1.0f), 0.5f);
    private static final BooleanProperty[] DIR_PROPS = {
            TachyonConduitBlock.NORTH, TachyonConduitBlock.SOUTH,
            TachyonConduitBlock.EAST, TachyonConduitBlock.WEST,
            TachyonConduitBlock.UP, TachyonConduitBlock.DOWN
    };

    private static final Direction[] DIRECTIONS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST,
            Direction.UP, Direction.DOWN
    };

    public TachyonConduitRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TachyonConduitBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!be.isShowingModeIndicators()) return;

        Level level = be.getLevel();
        if (level == null) return;

        long gameTime = level.getGameTime();
        BlockState state = be.getBlockState();
        double bx = be.getBlockPos().getX();
        double by = be.getBlockPos().getY();
        double bz = be.getBlockPos().getZ();

        for (int i = 0; i < DIRECTIONS.length; i++) {
            if (!state.getValue(DIR_PROPS[i])) continue;

            Direction dir = DIRECTIONS[i];

            // Check which resource types are active (non-DISABLED) on this side
            boolean energyActive = be.getSideConfig(dir, TYPE_ENERGY) != ConduitSideConfig.DISABLED;
            boolean itemActive = be.getSideConfig(dir, TYPE_ITEM) != ConduitSideConfig.DISABLED;
            boolean fluidActive = be.getSideConfig(dir, TYPE_FLUID) != ConduitSideConfig.DISABLED;

            if (!energyActive && !itemActive && !fluidActive) continue;

            // Stagger particle types across ticks so they don't all spawn at once
            // Energy on tick%6==0, Item on tick%6==2, Fluid on tick%6==4
            if (energyActive && gameTime % 6 == 0) {
                spawnParticleOnArm(level, bx, by, bz, dir, ENERGY_DUST);
            }
            if (itemActive && gameTime % 6 == 2) {
                spawnParticleOnArm(level, bx, by, bz, dir, ITEM_DUST);
            }
            if (fluidActive && gameTime % 6 == 4) {
                spawnParticleOnArm(level, bx, by, bz, dir, FLUID_DUST);
            }
        }
    }

    private void spawnParticleOnArm(Level level, double bx, double by, double bz,
                                     Direction dir, DustParticleOptions dust) {
        double px = bx + 0.5 + dir.getStepX() * 0.3;
        double py = by + 0.5 + dir.getStepY() * 0.3;
        double pz = bz + 0.5 + dir.getStepZ() * 0.3;

        // Small random jitter
        px += (level.random.nextDouble() - 0.5) * 0.12;
        py += (level.random.nextDouble() - 0.5) * 0.12;
        pz += (level.random.nextDouble() - 0.5) * 0.12;

        level.addParticle(dust, px, py, pz, 0, 0, 0);
    }
}
