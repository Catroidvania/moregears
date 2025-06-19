package com.catroidvania.moregears.mixins;

import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.tileentity.TileEntityBlastFurnace;
import net.minecraft.common.block.tileentity.TileEntityFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(TileEntityBlastFurnace.class)
public class TileEntityBlastFurnaceMixins {

    public TileEntityBlastFurnace thisTE = (TileEntityBlastFurnace)(Object)this;

    @Inject(method = "onInventoryChanged", at = @At("HEAD"))
    public void onInventoryChanged(CallbackInfo ci) {
        if (!thisTE.worldObj.isRemote) thisTE.worldObj.notifyBlocksOfNeighborChange(thisTE.xCoord, thisTE.yCoord, thisTE.zCoord, Blocks.FURNACE_ACTIVE.blockID);
    }
}
