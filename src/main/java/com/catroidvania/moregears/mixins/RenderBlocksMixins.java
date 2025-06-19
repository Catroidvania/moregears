package com.catroidvania.moregears.mixins;

import com.catroidvania.moregears.MoreGears;
import net.minecraft.client.renderer.world.RenderBlocks;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixins {
    
    public RenderBlocks thisRB = (RenderBlocks)(Object)this;

    @Accessor("uvRotateTop")
    public abstract void setUvRotateTop(int uvRotateTop);

    @Accessor("uvRotateBottom")
    public abstract void setUvRotateBottom(int uvRotateBottom);

    @Inject(method = "renderGearCircuitBlocks", at = @At(value = "INVOKE", target = "renderStandardBlock"), require = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onRenderGearCircuitBlocks(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> ci, int metadata) {
        if (block.blockID == MoreGears.COMPARATOR_IDLE.blockID || block.blockID == MoreGears.COMPARATOR_ACTIVE.blockID) {
            if (metadata != 1 && metadata != 5 && metadata != 9 && metadata != 13) {
                if (metadata != 3 && metadata != 7 && metadata != 11 && metadata != 15) {
                    if (metadata == 2 || metadata == 6 || metadata == 10 || metadata == 14) {
                        this.setUvRotateTop(3);
                        this.setUvRotateBottom(3);
                    }
                } else {
                    this.setUvRotateTop(2);
                    this.setUvRotateBottom(2);
                }
            } else {
                this.setUvRotateTop(1);
                this.setUvRotateBottom(1);
            }
        }
    }
}
