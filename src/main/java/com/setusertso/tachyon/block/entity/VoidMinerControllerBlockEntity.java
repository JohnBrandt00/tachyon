package com.setusertso.tachyon.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.setusertso.tachyon.ModItems;
import com.setusertso.tachyon.block.VoidMinerControllerBlock;
import com.setusertso.tachyon.block.VoidMinerPattern;
import com.setusertso.tachyon.init.ModBlockEntities;
import com.setusertso.tachyon.recipe.VoidMiningRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.items.ItemStackHandler;

import com.setusertso.tachyon.menu.VoidMinerMenu;

public class VoidMinerControllerBlockEntity extends BlockEntity implements MenuProvider {

    // Slot indices
    public static final int CATALYST_SLOT = 0;
    public static final int MODULE_SLOT_START = 1;
    public static final int MODULE_SLOT_END = 6;
    public static final int OUTPUT_SLOT_START = 7;
    public static final int OUTPUT_SLOT_END = 12;
    public static final int TOTAL_SLOTS = 13;

    // Tier-based constants
    private static final int[] ENERGY_PER_TICK = {0, 2_000, 5_000, 10_000, 20_000};
    private static final int[] ENERGY_CAPACITY = {0, 500_000, 1_000_000, 2_000_000, 5_000_000};
    private static final int[] PROCESS_TIME = {0, 400, 300, 200, 100};
    private static final int MAX_ENERGY_RECEIVE = 50_000;

    private static final int CATALYST_CYCLES = 10;

    // Storage
    private final ItemStackHandler items = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private CustomEnergyStorage energy = new CustomEnergyStorage(500_000, MAX_ENERGY_RECEIVE, 0);

    // State
    private boolean formed = false;
    private int structureTier = 0;
    private int progress = 0;
    private int catalystCycles = 0;
    private List<BlockPos> structurePositions = new ArrayList<>();
    private BlockPos riftCenter = null;
    private BlockPos structureOrigin = null;
    private BlockPos beamTop = null;
    private long lastProductionTick = -1;
    private ItemStack lastProducedItem = ItemStack.EMPTY;

    private final Random random = new Random();

    // Cached recipe list
    private List<VoidMiningRecipe> cachedRecipes = null;

    // Container data for GUI sync (8 values)
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> structureTier > 0 ? PROCESS_TIME[structureTier] : 0;
                case 2 -> energy.getEnergyStored() & 0xFFFF;
                case 3 -> (energy.getEnergyStored() >> 16) & 0xFFFF;
                case 4 -> energy.getMaxEnergyStored() & 0xFFFF;
                case 5 -> (energy.getMaxEnergyStored() >> 16) & 0xFFFF;
                case 6 -> catalystCycles;
                case 7 -> structureTier;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 6 -> catalystCycles = value;
                case 7 -> structureTier = value;
            }
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    public VoidMinerControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOID_MINER_CONTROLLER.get(), pos, state);
    }

    // --- Accessors ---

    public boolean isFormed() {
        return formed;
    }

    public int getStructureTier() {
        return structureTier;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public CustomEnergyStorage getEnergy() {
        return energy;
    }

    public BlockPos getRiftCenter() {
        return riftCenter;
    }

    public BlockPos getStructureOrigin() {
        return structureOrigin;
    }

    public BlockPos getBeamTop() {
        return beamTop;
    }

    public long getLastProductionTick() {
        return lastProductionTick;
    }

    public ItemStack getLastProducedItem() {
        return lastProducedItem;
    }

    public NonNullList<ItemStack> getDrops() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < items.getSlots(); i++) {
            if (!items.getStackInSlot(i).isEmpty()) {
                drops.add(items.getStackInSlot(i));
            }
        }
        return drops;
    }

    // --- Structure Management ---

    public void tryFormStructure() {
        if (level == null || level.isClientSide()) return;

        VoidMinerPattern.ValidationResult result = VoidMinerPattern.validate(level, worldPosition);
        if (result.valid()) {
            formed = true;
            structureTier = result.tier();
            structurePositions = new ArrayList<>(result.structurePositions());
            structureOrigin = result.origin();
            riftCenter = result.riftCenter();
            beamTop = result.beamTop();

            // Update energy storage for tier
            int newCapacity = ENERGY_CAPACITY[structureTier];
            int currentEnergy = Math.min(energy.getEnergyStored(), newCapacity);
            energy = new CustomEnergyStorage(newCapacity, MAX_ENERGY_RECEIVE, 0);
            energy.setEnergy(currentEnergy);

            // Clear recipe cache to rebuild with new tier
            cachedRecipes = null;

            // Set master pos on all slave entities
            for (BlockPos sPos : structurePositions) {
                BlockEntity be = level.getBlockEntity(sPos);
                if (be instanceof VoidFrameBlockEntity frame) {
                    frame.setMasterPos(worldPosition);
                } else if (be instanceof VoidMinerPortBlockEntity port) {
                    port.setMasterPos(worldPosition);
                }
            }

            // Update blockstate
            level.setBlock(worldPosition, getBlockState().setValue(VoidMinerControllerBlock.FORMED, true),
                    Block.UPDATE_ALL);
            setChanged();
            syncToClient();
        }
    }

    public void disassembleStructure() {
        if (level == null || level.isClientSide()) return;
        if (!formed) return;

        for (BlockPos sPos : structurePositions) {
            BlockEntity be = level.getBlockEntity(sPos);
            if (be instanceof VoidFrameBlockEntity frame) {
                frame.setMasterPos(null);
            } else if (be instanceof VoidMinerPortBlockEntity port) {
                port.setMasterPos(null);
            }
        }

        formed = false;
        structureTier = 0;
        structurePositions.clear();
        riftCenter = null;
        structureOrigin = null;
        beamTop = null;
        progress = 0;
        cachedRecipes = null;

        BlockState state = getBlockState();
        if (state.getValue(VoidMinerControllerBlock.FORMED)) {
            level.setBlock(worldPosition, state
                    .setValue(VoidMinerControllerBlock.FORMED, false)
                    .setValue(VoidMinerControllerBlock.ACTIVE, false),
                    Block.UPDATE_ALL);
        }
        setChanged();
        syncToClient();
    }

    public void onNeighborChanged(BlockPos changedPos) {
        if (level == null || level.isClientSide() || !formed) return;
        VoidMinerPattern.ValidationResult result = VoidMinerPattern.validate(level, worldPosition);
        if (!result.valid()) {
            disassembleStructure();
        }
    }

    // --- Processing ---

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                   VoidMinerControllerBlockEntity be) {
        if (!be.formed || be.structureTier == 0) return;

        boolean changed = false;
        boolean wasActive = state.getValue(VoidMinerControllerBlock.ACTIVE);

        if (be.canProcess()) {
            int energyCost = ENERGY_PER_TICK[be.structureTier];
            // Apply speed module reduction
            int maxProgress = be.getAdjustedProcessTime();

            be.energy.consumeEnergy(energyCost);
            be.progress++;
            changed = true;

            if (be.progress >= maxProgress) {
                be.produceOutput();
                be.progress = 0;

                // Consume catalyst cycle
                be.catalystCycles--;
                if (be.catalystCycles <= 0) {
                    // Try to consume another exotic matter
                    ItemStack catalyst = be.items.getStackInSlot(CATALYST_SLOT);
                    if (!catalyst.isEmpty() && catalyst.is(ModItems.EXOTIC_MATTER.get())) {
                        catalyst.shrink(1);
                        be.catalystCycles = CATALYST_CYCLES;
                    }
                }
            }

            if (!wasActive) {
                level.setBlock(pos, state.setValue(VoidMinerControllerBlock.ACTIVE, true), Block.UPDATE_ALL);
            }
        } else {
            if (be.progress > 0) {
                // Don't reset progress - pause (preserve progress without power)
                changed = true;
            }

            if (wasActive) {
                level.setBlock(pos, state.setValue(VoidMinerControllerBlock.ACTIVE, false), Block.UPDATE_ALL);
            }
        }

        if (changed) {
            be.setChanged();
        }
    }

    private boolean canProcess() {
        if (structureTier == 0) return false;

        // Need catalyst cycles
        if (catalystCycles <= 0) {
            // Try auto-consume
            ItemStack catalyst = items.getStackInSlot(CATALYST_SLOT);
            if (!catalyst.isEmpty() && catalyst.is(ModItems.EXOTIC_MATTER.get())) {
                catalyst.shrink(1);
                catalystCycles = CATALYST_CYCLES;
            } else {
                return false;
            }
        }

        // Need energy
        int energyCost = ENERGY_PER_TICK[structureTier];
        if (energy.getEnergyStored() < energyCost) return false;

        // Need output space
        return hasOutputSpace();
    }

    private boolean hasOutputSpace() {
        for (int i = OUTPUT_SLOT_START; i <= OUTPUT_SLOT_END; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private int getAdjustedProcessTime() {
        int baseTime = PROCESS_TIME[structureTier];
        int speedModules = countModule(ModItems.SPEED_MODULE.get());
        // Each speed module item in a stack counts — 64 speed modules = very fast
        // Formula: baseTime / (1 + totalCount * 0.5)
        // 1 module = /1.5, 64 = /33, 384 (6 slots) = /193
        float divisor = 1.0f + speedModules * 0.5f;
        return Math.max(2, Math.round(baseTime / divisor));
    }

    private int getFortuneBonus() {
        // Each fortune module item gives +1 extra item per production, up to 6 slots * 64 = 384 max
        // Cap at a reasonable amount
        return Math.min(10, countModule(ModItems.FORTUNE_MODULE.get()));
    }

    /**
     * Count total module items across all module slots (counts stack sizes, not just slot presence).
     */
    private int countModule(net.minecraft.world.item.Item moduleItem) {
        int count = 0;
        for (int i = MODULE_SLOT_START; i <= MODULE_SLOT_END; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.is(moduleItem)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    // --- Loot System ---

    private void produceOutput() {
        if (level == null) return;

        List<VoidMiningRecipe> recipes = getAvailableRecipes();
        if (recipes.isEmpty()) return;

        // Build weighted pool
        List<WeightedDrop> pool = new ArrayList<>();
        double totalWeight = 0;

        for (VoidMiningRecipe recipe : recipes) {
            // Skip drops not allowed in this dimension without the right module
            if (!isDimensionAllowed(recipe.category())) continue;

            double weight = recipe.weight();

            // Apply category module bonuses
            weight *= getCategoryMultiplier(recipe.category());

            pool.add(new WeightedDrop(recipe, weight));
            totalWeight += weight;
        }

        if (totalWeight <= 0 || pool.isEmpty()) return;

        // Roll 1 + fortune bonus items
        int itemCount = 1 + getFortuneBonus();

        for (int i = 0; i < itemCount; i++) {
            VoidMiningRecipe selected = selectWeightedRandom(pool, totalWeight);
            if (selected == null) continue;

            int count = selected.minCount() + random.nextInt(selected.maxCount() - selected.minCount() + 1);

            // Silk Touch: if module installed and recipe has a silk touch alternative, use that
            boolean hasSilkTouch = hasModule(ModItems.SILK_TOUCH_MODULE.get());
            net.minecraft.world.item.Item outputItem = (hasSilkTouch && selected.hasSilkTouchAlternative())
                    ? selected.silkTouchItem()
                    : selected.resultItem();
            ItemStack output = new ItemStack(outputItem, count);

            // Track the first produced item for visual effect
            if (i == 0) {
                recordProducedItem(output);
            }

            // Try to insert into output slots
            insertOutput(output);
        }

        // Record production time and last item for visual effects
        // Only update if previous item-fall animation has finished (120 ticks)
        // so fast production doesn't constantly reset the animation
        if (level != null) {
            long now = level.getGameTime();
            if (lastProductionTick < 0 || now - lastProductionTick >= 120) {
                lastProductionTick = now;
                syncToClient();
            }
        }
    }

    /** Store what item was produced (call before insertOutput loop ends). */
    private void recordProducedItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            lastProducedItem = stack.copy();
        }
    }

    private VoidMiningRecipe selectWeightedRandom(List<WeightedDrop> pool, double totalWeight) {
        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (WeightedDrop drop : pool) {
            cumulative += drop.weight;
            if (roll < cumulative) {
                return drop.recipe;
            }
        }
        return pool.get(pool.size() - 1).recipe;
    }

    private void insertOutput(ItemStack stack) {
        for (int i = OUTPUT_SLOT_START; i <= OUTPUT_SLOT_END; i++) {
            stack = items.insertItem(i, stack, false);
            if (stack.isEmpty()) break;
        }
    }

    private double getCategoryMultiplier(String category) {
        double multiplier = 1.0;
        for (int i = MODULE_SLOT_START; i <= MODULE_SLOT_END; i++) {
            ItemStack module = items.getStackInSlot(i);
            if (module.isEmpty()) continue;

            if (module.is(ModItems.ORE_EXTRACTION_MODULE.get()) && "common_ore".equals(category)) {
                multiplier *= 1.5;
            } else if (module.is(ModItems.RARE_EARTH_MODULE.get()) && "mod_ore".equals(category)) {
                multiplier *= 1.5;
            } else if (module.is(ModItems.NETHER_SIPHON_MODULE.get()) && "nether".equals(category)) {
                multiplier *= 1.5;
            } else if (module.is(ModItems.END_SIPHON_MODULE.get()) && "end".equals(category)) {
                multiplier *= 1.5;
            } else if (module.is(ModItems.EXOTIC_ATTUNEMENT_MODULE.get()) && "ultra_rare".equals(category)) {
                multiplier *= 1.5;
            }
        }
        return multiplier;
    }

    /**
     * Check if a drop category is allowed based on current dimension and installed modules.
     * Nether drops require being in the Nether OR having a Nether Siphon Module.
     * End drops require being in the End OR having an End Siphon Module.
     */
    private boolean isDimensionAllowed(String category) {
        if (level == null) return true;
        if ("nether".equals(category)) {
            return level.dimension() == Level.NETHER || hasModule(ModItems.NETHER_SIPHON_MODULE.get());
        }
        if ("end".equals(category)) {
            return level.dimension() == Level.END || hasModule(ModItems.END_SIPHON_MODULE.get());
        }
        return true;
    }

    private boolean hasModule(net.minecraft.world.item.Item moduleItem) {
        for (int i = MODULE_SLOT_START; i <= MODULE_SLOT_END; i++) {
            if (items.getStackInSlot(i).is(moduleItem)) {
                return true;
            }
        }
        return false;
    }

    private List<VoidMiningRecipe> getAvailableRecipes() {
        if (cachedRecipes != null) return cachedRecipes;
        if (level == null) return List.of();

        RecipeManager recipeManager = level.getRecipeManager();
        List<VoidMiningRecipe> allRecipes = recipeManager
                .getAllRecipesFor(com.setusertso.tachyon.init.ModRecipes.VOID_MINING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        // Filter by tier
        cachedRecipes = allRecipes.stream()
                .filter(r -> r.requiredTier() <= structureTier)
                .toList();

        return cachedRecipes;
    }

    // --- Module validation ---

    public static boolean isVoidMinerModule(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ModItems.ORE_EXTRACTION_MODULE.get())
                || stack.is(ModItems.SILK_TOUCH_MODULE.get())
                || stack.is(ModItems.RARE_EARTH_MODULE.get())
                || stack.is(ModItems.NETHER_SIPHON_MODULE.get())
                || stack.is(ModItems.END_SIPHON_MODULE.get())
                || stack.is(ModItems.EXOTIC_ATTUNEMENT_MODULE.get())
                || stack.is(ModItems.SPEED_MODULE.get())
                || stack.is(ModItems.FORTUNE_MODULE.get());
    }

    // --- Menu ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.tachyon.void_miner");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new VoidMinerMenu(containerId, playerInventory, items, dataAccess);
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.putInt("CatalystCycles", catalystCycles);
        tag.putBoolean("Formed", formed);
        tag.putInt("StructureTier", structureTier);

        tag.putLong("LastProductionTick", lastProductionTick);

        if (!lastProducedItem.isEmpty()) {
            tag.put("LastProducedItem", lastProducedItem.save(registries));
        }

        if (riftCenter != null) {
            tag.putInt("RiftX", riftCenter.getX());
            tag.putInt("RiftY", riftCenter.getY());
            tag.putInt("RiftZ", riftCenter.getZ());
        }

        if (structureOrigin != null) {
            tag.putInt("OriginX", structureOrigin.getX());
            tag.putInt("OriginY", structureOrigin.getY());
            tag.putInt("OriginZ", structureOrigin.getZ());
        }

        if (beamTop != null) {
            tag.putInt("BeamTopX", beamTop.getX());
            tag.putInt("BeamTopY", beamTop.getY());
            tag.putInt("BeamTopZ", beamTop.getZ());
        }

        if (!structurePositions.isEmpty()) {
            ListTag posList = new ListTag();
            for (BlockPos pos : structurePositions) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("X", pos.getX());
                posTag.putInt("Y", pos.getY());
                posTag.putInt("Z", pos.getZ());
                posList.add(posTag);
            }
            tag.put("StructurePositions", posList);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Items"));
        progress = tag.getInt("Progress");
        catalystCycles = tag.getInt("CatalystCycles");
        formed = tag.getBoolean("Formed");
        structureTier = tag.getInt("StructureTier");

        // Rebuild energy storage with correct capacity
        int capacity = structureTier > 0 && structureTier < ENERGY_CAPACITY.length
                ? ENERGY_CAPACITY[structureTier] : 500_000;
        energy = new CustomEnergyStorage(capacity, MAX_ENERGY_RECEIVE, 0);
        energy.setEnergy(tag.getInt("Energy"));

        lastProductionTick = tag.getLong("LastProductionTick");

        if (tag.contains("LastProducedItem")) {
            lastProducedItem = ItemStack.parse(registries, tag.getCompound("LastProducedItem")).orElse(ItemStack.EMPTY);
        }

        if (tag.contains("RiftX")) {
            riftCenter = new BlockPos(tag.getInt("RiftX"), tag.getInt("RiftY"), tag.getInt("RiftZ"));
        }

        if (tag.contains("OriginX")) {
            structureOrigin = new BlockPos(tag.getInt("OriginX"), tag.getInt("OriginY"), tag.getInt("OriginZ"));
        }

        if (tag.contains("BeamTopX")) {
            beamTop = new BlockPos(tag.getInt("BeamTopX"), tag.getInt("BeamTopY"), tag.getInt("BeamTopZ"));
        }

        structurePositions.clear();
        if (tag.contains("StructurePositions")) {
            ListTag posList = tag.getList("StructurePositions", Tag.TAG_COMPOUND);
            for (int i = 0; i < posList.size(); i++) {
                CompoundTag posTag = posList.getCompound(i);
                structurePositions.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        }

        // Clear recipe cache on load
        cachedRecipes = null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- Inner classes ---

    private record WeightedDrop(VoidMiningRecipe recipe, double weight) {}
}
