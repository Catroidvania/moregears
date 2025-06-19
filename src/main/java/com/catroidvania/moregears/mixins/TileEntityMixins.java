package com.catroidvania.moregears.mixins;

import net.minecraft.common.block.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntity.class)
public class TileEntityMixins {

    TileEntity thisTE = (TileEntity)(Object)this;

    @Inject(method = "updateEntity", at = @At("HEAD"))
    public void onUpdateEntity(CallbackInfo ci) {
        if (!thisTE.worldObj.isRemote) thisTE.worldObj.notifyBlocksOfNeighborChange(thisTE.xCoord, thisTE.yCoord, thisTE.zCoord, 0);
    }
}
