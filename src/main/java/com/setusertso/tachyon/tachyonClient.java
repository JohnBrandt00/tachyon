package com.setusertso.tachyon;

import com.setusertso.tachyon.init.ModMenuTypes;
import com.setusertso.tachyon.screen.SuperluminalEmitterScreen;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = tachyon.MODID, dist = Dist.CLIENT)
public class tachyonClient {
    public tachyonClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterMenuScreens);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        tachyon.LOGGER.info("HELLO FROM CLIENT SETUP");
        tachyon.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    private void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SUPERLUMINAL_EMITTER.get(), SuperluminalEmitterScreen::new);
    }
}
