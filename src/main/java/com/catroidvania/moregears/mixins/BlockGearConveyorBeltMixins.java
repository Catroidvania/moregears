package com.catroidvania.moregears.mixins;

import com.catroidvania.moregears.BlockGearFunnel;
import com.catroidvania.moregears.MoreGears;
import net.minecraft.common.block.children.BlockGearConveyorBelt;
import net.minecraft.common.entity.Entity;
import net.minecraft.common.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(BlockGearConveyorBelt.class)
public class BlockGearConveyorBeltMixins {

    @Inject(method = "onEntityCollidedWithBlock", at = @At("HEAD"))
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity, CallbackInfo ci) {
        if (world.getBlockId(x, y-1, z) == MoreGears.FUNNEL_IDLE.blockID &&
                BlockGearFunnel.getOrientation(world.getBlockMetadata(x, y-1, z)) == 1) {
            MoreGears.FUNNEL_IDLE.onEntityCollidedWithBlock(world, x, y-1, z, entity);
        }
    }
}
