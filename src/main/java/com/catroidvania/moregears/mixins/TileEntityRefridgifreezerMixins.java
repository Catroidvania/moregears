package com.catroidvania.moregears.mixins;

import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.tileentity.TileEntityFurnace;
import net.minecraft.common.block.tileentity.TileEntityRefridgifreezer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(TileEntityRefridgifreezer.class)
public class TileEntityRefridgifreezerMixins {

    public TileEntityRefridgifreezer thisTE = (TileEntityRefridgifreezer)(Object)this;

    @Inject(method = "onInventoryChanged", at = @At("HEAD"))
    public void onInventoryChanged(CallbackInfo ci) {
        if (!thisTE.worldObj.isRemote) thisTE.worldObj.notifyBlocksOfNeighborChange(thisTE.xCoord, thisTE.yCoord, thisTE.zCoord, Blocks.REFRIDGIFREEZER_ACTIVE.blockID);
    }
}
