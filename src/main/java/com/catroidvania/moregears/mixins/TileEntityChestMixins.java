package com.catroidvania.moregears.mixins;

import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.tileentity.TileEntityChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(TileEntityChest.class)
public class TileEntityChestMixins {

    public TileEntityChest thisTEC = (TileEntityChest)(Object)this;

    @Inject(method = "onInventoryChanged", at = @At("HEAD"))
    public void onInventoryChanged(CallbackInfo ci) {
        if (!thisTEC.worldObj.isRemote) thisTEC.worldObj.notifyBlocksOfNeighborChange(thisTEC.xCoord, thisTEC.yCoord, thisTEC.zCoord, Blocks.CHEST.blockID);
    }
}
