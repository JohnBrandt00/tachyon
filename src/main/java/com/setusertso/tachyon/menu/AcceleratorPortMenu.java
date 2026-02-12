package com.setusertso.tachyon.menu;

import com.setusertso.tachyon.block.entity.AcceleratorPortBlockEntity;
import com.setusertso.tachyon.block.entity.PortMode;
import com.setusertso.tachyon.init.ModMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class AcceleratorPortMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final BlockPos portPos;

    // Client constructor (from IMenuTypeExtension)
    public AcceleratorPortMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.ACCELERATOR_PORT.get(), containerId);
        this.portPos = pos;
        this.data = new SimpleContainerData(1);
        this.addDataSlots(data);
    }

    // Server constructor
    public AcceleratorPortMenu(int containerId, Inventory playerInventory, AcceleratorPortBlockEntity be) {
        super(ModMenuTypes.ACCELERATOR_PORT.get(), containerId);
        this.portPos = be.getBlockPos();
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return index == 0 ? be.getMode().ordinal() : 0;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
        this.addDataSlots(data);
    }

    public BlockPos getPortPos() {
        return portPos;
    }

    public PortMode getMode() {
        return PortMode.fromOrdinal(data.get(0));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
