package com.catroidvania.moregears.mixins;

import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.tileentity.TileEntityDrawer;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(TileEntityDrawer.class)
public class TileEntityDrawerMixins {

    public TileEntityDrawer thisTED = (TileEntityDrawer)(Object)this;

    @Inject(method = "clickEvent", at = @At("RETURN"))
    public void onClickEvent(boolean leftclick, World world, int x, int y, int z, EntityPlayer player, CallbackInfo ci) {
        if (!thisTED.worldObj.isRemote) thisTED.worldObj.notifyBlocksOfNeighborChange(thisTED.xCoord, thisTED.yCoord, thisTED.zCoord, Blocks.DRAWER.blockID);
    }
}
