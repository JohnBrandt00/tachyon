package com.setusertso.tachyon.init;

import java.util.function.Supplier;

import com.setusertso.tachyon.tachyon;
import com.setusertso.tachyon.block.entity.PhotonicInjectorMenu;
import com.setusertso.tachyon.menu.AcceleratorControllerMenu;
import com.setusertso.tachyon.menu.AcceleratorPortMenu;
import com.setusertso.tachyon.menu.SingularityControllerMenu;
import com.setusertso.tachyon.menu.SingularityPortMenu;
import com.setusertso.tachyon.menu.SuperluminalEmitterMenu;
import com.setusertso.tachyon.menu.ThoriumReactorMenu;
import com.setusertso.tachyon.menu.SolarCollectorMenu;
import com.setusertso.tachyon.menu.PhotonCompressorMenu;
import com.setusertso.tachyon.menu.OreCrusherMenu;
import com.setusertso.tachyon.menu.AlloyForgeMenu;
import com.setusertso.tachyon.menu.VoidMinerMenu;
import com.setusertso.tachyon.menu.CondenserControllerMenu;

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

    public static final Supplier<MenuType<SingularityControllerMenu>> SINGULARITY_CONTROLLER =
            MENUS.register("singularity_controller",
                    () -> IMenuTypeExtension.create(
                            (containerId, inv, buf) -> new SingularityControllerMenu(containerId, inv, buf.readBlockPos())));

    public static final Supplier<MenuType<SingularityPortMenu>> SINGULARITY_PORT =
            MENUS.register("singularity_port",
                    () -> IMenuTypeExtension.create(
                            (containerId, inv, buf) -> new SingularityPortMenu(containerId, inv, buf.readBlockPos())));

    public static final Supplier<MenuType<PhotonicInjectorMenu>> PHOTONIC_INJECTOR =
            MENUS.register("photonic_injector",
                    () -> IMenuTypeExtension.create(
                            (containerId, inv, buf) -> new PhotonicInjectorMenu(containerId, inv, buf.readBlockPos())));

    public static final Supplier<MenuType<ThoriumReactorMenu>> THORIUM_REACTOR =
            MENUS.register("thorium_reactor",
                    () -> new MenuType<>(ThoriumReactorMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<SolarCollectorMenu>> SOLAR_COLLECTOR =
            MENUS.register("solar_collector",
                    () -> new MenuType<>(SolarCollectorMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<PhotonCompressorMenu>> PHOTON_COMPRESSOR =
            MENUS.register("photon_compressor",
                    () -> new MenuType<>(PhotonCompressorMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<OreCrusherMenu>> ORE_CRUSHER =
            MENUS.register("ore_crusher",
                    () -> new MenuType<>(OreCrusherMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<AlloyForgeMenu>> ALLOY_FORGE =
            MENUS.register("alloy_forge",
                    () -> new MenuType<>(AlloyForgeMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<VoidMinerMenu>> VOID_MINER =
            MENUS.register("void_miner",
                    () -> new MenuType<>(VoidMinerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<CondenserControllerMenu>> CONDENSER_CONTROLLER =
            MENUS.register("condenser_controller",
                    () -> new MenuType<>(CondenserControllerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
