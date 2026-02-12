package com.setusertso.tachyon.init;

import java.util.function.Supplier;

import com.setusertso.tachyon.tachyon;
import com.setusertso.tachyon.menu.AcceleratorControllerMenu;
import com.setusertso.tachyon.menu.AcceleratorPortMenu;
import com.setusertso.tachyon.menu.SuperluminalEmitterMenu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, tachyon.MODID);

    public static final Supplier<MenuType<SuperluminalEmitterMenu>> SUPERLUMINAL_EMITTER =
            MENUS.register("superluminal_emitter",
                    () -> new MenuType<>(SuperluminalEmitterMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<AcceleratorControllerMenu>> ACCELERATOR_CONTROLLER =
            MENUS.register("accelerator_controller",
                    () -> new MenuType<>(AcceleratorControllerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<AcceleratorPortMenu>> ACCELERATOR_PORT =
            MENUS.register("accelerator_port",
                    () -> IMenuTypeExtension.create(
                            (containerId, inv, buf) -> new AcceleratorPortMenu(containerId, inv, buf.readBlockPos())));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
