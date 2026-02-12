package com.setusertso.tachyon.block.entity;

import org.jetbrains.annotations.NotNull;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class OutputOnlyFluidHandler implements IFluidHandler {
    private final IFluidTank inner;

    public OutputOnlyFluidHandler(IFluidTank inner) {
        this.inner = inner;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return inner.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return inner.getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return inner.isFluidValid(stack);
    }

    @Override
    public int fill(@NotNull FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, FluidAction action) {
        return inner.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return inner.drain(maxDrain, action);
    }
}
