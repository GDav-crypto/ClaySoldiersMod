/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.claysoldiers.registry.upgrade.hand;

import de.sanandrew.mods.claysoldiers.api.CsmConstants;
import de.sanandrew.mods.claysoldiers.api.attribute.AttributeHelper;
import de.sanandrew.mods.claysoldiers.api.entity.soldier.upgrade.IHandedUpgradeable;
import de.sanandrew.mods.claysoldiers.api.entity.soldier.ISoldier;
import de.sanandrew.mods.claysoldiers.api.entity.soldier.upgrade.EnumUpgFunctions;
import de.sanandrew.mods.claysoldiers.api.entity.soldier.upgrade.ISoldierUpgrade;
import de.sanandrew.mods.claysoldiers.api.entity.soldier.upgrade.ISoldierUpgradeInst;
import de.sanandrew.mods.claysoldiers.api.entity.soldier.upgrade.EnumUpgradeType;
import de.sanandrew.mods.claysoldiers.api.entity.soldier.upgrade.UpgradeFunctions;
import de.sanandrew.mods.claysoldiers.entity.attributes.AttributeModifierRnd;
import de.sanandrew.mods.claysoldiers.registry.upgrade.Upgrades;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.UUID;

@UpgradeFunctions({EnumUpgFunctions.ON_ATTACK_SUCCESS, EnumUpgFunctions.ON_DEATH})
public class UpgradeBlazeRod
        implements ISoldierUpgrade
{
    private static final ItemStack[] UPG_ITEMS = { new ItemStack(Items.BLAZE_ROD, 1) };
    private static final byte MAX_USAGES = 20;

    @Override
    public String getModId() {
        return CsmConstants.ID;
    }

    @Override
    public String getShortName() {
        return "blazerod";
    }

    @Override
    @Nonnull
    public ItemStack[] getStacks() {
        return UPG_ITEMS;
    }

    @Nonnull
    @Override
    public EnumUpgradeType getType(IHandedUpgradeable checker) {
        return EnumUpgradeType.MAIN_HAND;
    }

    @Override
    public boolean syncData() {
        return true;
    }

    @Override
    public void onAdded(ISoldier<?> soldier, ItemStack stack, ISoldierUpgradeInst upgradeInst) {
        if( !soldier.getEntity().world.isRemote ) {
            upgradeInst.getNbtData().setByte("uses", MAX_USAGES);
            AttributeHelper.tryApplyAttackDmgModifier(soldier.getEntity(), ATTACK_DMG);
            soldier.getEntity().playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.2F, ((MiscUtils.RNG.randomFloat() - MiscUtils.RNG.randomFloat()) * 0.7F + 1.0F) * 2.0F);
            stack.shrink(1);
        }
    }

    @Override
    public void onAttackSuccess(ISoldier<?> soldier, ISoldierUpgradeInst upgradeInst, Entity target) {
        byte uses = (byte) (upgradeInst.getNbtData().getByte("uses") - 1);
        target.setFire(soldier.hasUpgrade(Upgrades.EM_COAL, EnumUpgradeType.ENHANCEMENT) ? 6 : 3);
        if( uses < 1 ) {
            AttributeHelper.tryRemoveAttackDmgModifier(soldier.getEntity(), ATTACK_DMG);
            soldier.destroyUpgrade(upgradeInst.getUpgrade(), upgradeInst.getUpgradeType(), false);
            soldier.getEntity().playSound(SoundEvents.ENTITY_ITEM_BREAK, 0.8F, 0.8F + MiscUtils.RNG.randomFloat() * 0.4F);
        } else {
            upgradeInst.getNbtData().setByte("uses", uses);
        }
    }

    @Override
    public void onDeath(ISoldier<?> soldier, ISoldierUpgradeInst upgradeInst, DamageSource dmgSource, NonNullList<ItemStack> drops) {
        if( upgradeInst.getNbtData().getByte("uses") >= MAX_USAGES ) {
            drops.add(upgradeInst.getSavedStack());
        }
    }

    public static final AttributeModifier ATTACK_DMG = new AttributeModifierRnd(UUID.fromString("BA1AC198-AAB5-404B-A6E6-CE6A7E893207"), CsmConstants.ID + ".blazerod_upg", 1.0D, 1.0D, 0);
}
