package dev.the_fireplace.overlord.impl.world;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.overlord.domain.world.UndeadDaylightDamager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.Random;

@Implementation
public final class DaylightDamager implements UndeadDaylightDamager {
    private final Random random = new Random();

    @Override
    public void applyDamage(LivingEntity entity) {
        if (entity.getEntityWorld().isClient()) {
            return;
        }
        ItemStack helmetStack = entity.getEquippedStack(EquipmentSlot.HEAD);
        if (!helmetStack.isEmpty()) {
            if (!helmetStack.isDamageable()) {
                return;
            }
            helmetStack.setDamage(helmetStack.getDamage() + random.nextInt(2));
            if (helmetStack.getDamage() >= helmetStack.getMaxDamage()) {
                entity.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                entity.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
        } else {
            entity.setOnFireFor(8);
        }
    }
}
