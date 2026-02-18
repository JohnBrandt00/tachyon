package com.setusertso.tachyon.item;

import com.setusertso.tachyon.tachyon;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.resources.ResourceLocation;

@EventBusSubscriber(modid = tachyon.MODID)
public class TachyonArmorEvents {

    private static final ResourceLocation TACHYON_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "tachyon_leggings_speed");
    private static final ResourceLocation TACHYON_ATTACK_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "tachyon_set_attack_speed");
    private static final ResourceLocation TACHYON_STEP_HEIGHT_ID =
            ResourceLocation.fromNamespaceAndPath(tachyon.MODID, "tachyon_set_step_height");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        boolean hasHelmet = isWearingTachyonArmor(player, EquipmentSlot.HEAD);
        boolean hasChestplate = isWearingTachyonArmor(player, EquipmentSlot.CHEST);
        boolean hasLeggings = isWearingTachyonArmor(player, EquipmentSlot.LEGS);
        boolean hasBoots = isWearingTachyonArmor(player, EquipmentSlot.FEET);
        boolean hasFullSet = hasHelmet && hasChestplate && hasLeggings && hasBoots;

        // Helmet: Night Vision
        if (hasHelmet) {
            // Refresh every 210 ticks (10.5 seconds) to prevent the flickering at the end
            MobEffectInstance currentNV = player.getEffect(MobEffects.NIGHT_VISION);
            if (currentNV == null || currentNV.getDuration() < 210) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, true));
            }
        }

        // Leggings: +10% movement speed
        if (hasLeggings) {
            if (!player.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(TACHYON_SPEED_ID)) {
                player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
                        new AttributeModifier(TACHYON_SPEED_ID, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else {
            player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(TACHYON_SPEED_ID);
        }

        // Full Set Bonus: +15% attack speed + step assist
        if (hasFullSet) {
            if (!player.getAttribute(Attributes.ATTACK_SPEED).hasModifier(TACHYON_ATTACK_SPEED_ID)) {
                player.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(
                        new AttributeModifier(TACHYON_ATTACK_SPEED_ID, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            // Step assist via STEP_HEIGHT attribute (+0.4 to reach 1.0 total from default 0.6)
            if (!player.getAttribute(Attributes.STEP_HEIGHT).hasModifier(TACHYON_STEP_HEIGHT_ID)) {
                player.getAttribute(Attributes.STEP_HEIGHT).addTransientModifier(
                        new AttributeModifier(TACHYON_STEP_HEIGHT_ID, 0.4, AttributeModifier.Operation.ADD_VALUE));
            }
        } else {
            player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(TACHYON_ATTACK_SPEED_ID);
            player.getAttribute(Attributes.STEP_HEIGHT).removeModifier(TACHYON_STEP_HEIGHT_ID);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isWearingTachyonArmor(player, EquipmentSlot.FEET)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean isWearingTachyonArmor(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        return stack.getItem() instanceof TachyonArmorItem;
    }
}
