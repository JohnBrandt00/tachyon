package com.setusertso.tachyon;

import com.setusertso.tachyon.client.AcceleratorControllerRenderer;
import com.setusertso.tachyon.client.PhotonicInjectorRenderer;
import com.setusertso.tachyon.client.SingularityControllerRenderer;
import com.setusertso.tachyon.client.SingularityDebugRenderer;
import com.setusertso.tachyon.client.VoidMinerRiftRenderer;
import com.setusertso.tachyon.client.particle.AccretionDiskParticle;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.init.ModFluids;
import com.setusertso.tachyon.init.ModMenuTypes;
import com.setusertso.tachyon.init.ModParticles;
import com.setusertso.tachyon.screen.AcceleratorControllerScreen;
import com.setusertso.tachyon.screen.AcceleratorPortScreen;
import com.setusertso.tachyon.screen.PhotonicInjectorScreen;
import com.setusertso.tachyon.screen.SingularityControllerScreen;
import com.setusertso.tachyon.screen.SingularityPortScreen;
import com.setusertso.tachyon.screen.SuperluminalEmitterScreen;
import com.setusertso.tachyon.screen.ThoriumReactorScreen;
import com.setusertso.tachyon.screen.SolarCollectorScreen;
import com.setusertso.tachyon.screen.PhotonCompressorScreen;
import com.setusertso.tachyon.screen.OreCrusherScreen;
import com.setusertso.tachyon.screen.AlloyForgeScreen;
import com.setusertso.tachyon.screen.VoidMinerScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = tachyon.MODID, dist = Dist.CLIENT)
public class tachyonClient {
    public tachyonClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterMenuScreens);
        modEventBus.addListener(this::onRegisterRenderers);
        modEventBus.addListener(this::onRegisterClientExtensions);
        modEventBus.addListener(this::onRegisterParticleProviders);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        tachyon.LOGGER.info("HELLO FROM CLIENT SETUP");
        tachyon.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    private void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SUPERLUMINAL_EMITTER.get(), SuperluminalEmitterScreen::new);
        event.register(ModMenuTypes.ACCELERATOR_CONTROLLER.get(), AcceleratorControllerScreen::new);
        event.register(ModMenuTypes.ACCELERATOR_PORT.get(), AcceleratorPortScreen::new);
        event.register(ModMenuTypes.SINGULARITY_CONTROLLER.get(), SingularityControllerScreen::new);
        event.register(ModMenuTypes.SINGULARITY_PORT.get(), SingularityPortScreen::new);
        event.register(ModMenuTypes.PHOTONIC_INJECTOR.get(), PhotonicInjectorScreen::new);
        event.register(ModMenuTypes.THORIUM_REACTOR.get(), ThoriumReactorScreen::new);
        event.register(ModMenuTypes.SOLAR_COLLECTOR.get(), SolarCollectorScreen::new);
        event.register(ModMenuTypes.PHOTON_COMPRESSOR.get(), PhotonCompressorScreen::new);
        event.register(ModMenuTypes.ORE_CRUSHER.get(), OreCrusherScreen::new);
        event.register(ModMenuTypes.ALLOY_FORGE.get(), AlloyForgeScreen::new);
        event.register(ModMenuTypes.VOID_MINER.get(), VoidMinerScreen::new);
    }

    private void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ACCELERATOR_CONTROLLER.get(),
                AcceleratorControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SINGULARITY_DEBUG.get(),
                SingularityDebugRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SINGULARITY_CONTROLLER.get(),
                SingularityControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PHOTONIC_INJECTOR.get(),
                PhotonicInjectorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VOID_MINER_CONTROLLER.get(),
                VoidMinerRiftRenderer::new);
    }

    private void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.ACCRETION_DISK.get(), AccretionDiskParticle.Provider::new);
    }

    private void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow");
            }

            @Override
            public int getTintColor() {
                return 0xCC9955DD; // Translucent purple for helium gas
            }
        }, ModFluids.HELIUM_TYPE.get());
    }
}
