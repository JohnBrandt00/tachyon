package com.setusertso.tachyon.block.entity;

import net.neoforged.neoforge.energy.EnergyStorage;

public class CustomEnergyStorage extends EnergyStorage {

    public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    public int consumeEnergy(int amount) {
        int consumed = Math.min(this.energy, amount);
        this.energy -= consumed;
        return consumed;
    }

    public void addEnergy(int amount) {
        this.energy = Math.min(this.capacity, this.energy + amount);
    }
}
