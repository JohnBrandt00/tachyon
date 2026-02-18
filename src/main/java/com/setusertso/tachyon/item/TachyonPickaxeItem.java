package com.setusertso.tachyon.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class TachyonPickaxeItem extends PickaxeItem {

    public TachyonPickaxeItem(Properties properties) {
        super(ModToolTier.TACHYON_ALLOY, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            // Auto-smelt: check if silk touch is NOT on
            if (level instanceof ServerLevel serverLevel) {
                var enchantments = stack.getEnchantments();
                // Check if silk touch is present by looking at the stack's enchantments
                boolean hasSilkTouch = false;
                var silkTouchHolder = serverLevel.registryAccess()
                        .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                        .get(Enchantments.SILK_TOUCH);
                if (silkTouchHolder.isPresent()) {
                    hasSilkTouch = enchantments.getLevel(silkTouchHolder.get()) > 0;
                }

                if (!hasSilkTouch) {
                    // Get the block's normal drop item
                    var blockDrops = net.minecraft.world.level.block.Block.getDrops(state, serverLevel, pos, null, miner, stack);
                    for (var drop : blockDrops) {
                        SingleRecipeInput input = new SingleRecipeInput(drop);
                        Optional<net.minecraft.world.item.crafting.RecipeHolder<SmeltingRecipe>> recipe =
                                serverLevel.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, serverLevel);
                        if (recipe.isPresent()) {
                            ItemStack smeltedResult = recipe.get().value().getResultItem(serverLevel.registryAccess()).copy();
                            if (!smeltedResult.isEmpty()) {
                                // Apply fortune multiplier
                                var fortuneHolder = serverLevel.registryAccess()
                                        .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                                        .get(Enchantments.FORTUNE);
                                int fortuneLevel = 0;
                                if (fortuneHolder.isPresent()) {
                                    fortuneLevel = enchantments.getLevel(fortuneHolder.get());
                                }
                                int bonusCount = fortuneLevel > 0 ? level.getRandom().nextInt(fortuneLevel + 1) : 0;
                                smeltedResult.setCount(drop.getCount() + bonusCount);

                                // Remove the original drops and replace with smelted
                                net.minecraft.world.level.block.Block.popResource(level, pos, smeltedResult);
                            }
                        } else {
                            // No smelting recipe, drop normally
                            net.minecraft.world.level.block.Block.popResource(level, pos, drop);
                        }
                    }
                    // Prevent the default drops
                    level.removeBlock(pos, false);
                    stack.hurtAndBreak(1, miner, EquipmentSlot.MAINHAND);
                    return true;
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, miner);
    }
}
