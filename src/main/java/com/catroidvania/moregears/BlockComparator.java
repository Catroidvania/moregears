package com.catroidvania.moregears;

import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.children.BlockContainer;
import net.minecraft.common.block.children.BlockGearWait;
import net.minecraft.common.block.sound.StepSounds;
import net.minecraft.common.block.tileentity.*;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.data.EnumTools;
import net.minecraft.common.world.World;

import java.awt.*;
import java.util.Random;


public class BlockComparator extends BlockGearWait {
    // good nuff
    public static final float[] thresholds = new float[]{0.0f, 0.33333f, 0.66666f, 0.99999f};

    public BlockComparator(String id, boolean state) {
        super(id, state);
        this.setBlockName("comparator");
        this.setSound(StepSounds.SOUND_NETHERRACK);
        this.setHardness(0.7F);
        this.setTooltipColor(Color.GRAY.getRGB());
        this.setEffectiveTool(EnumTools.PICKAXE);
    }

    public void updateComparator(World world, int x, int y, int z) {
        int metadata = world.getBlockMetadata(x, y, z);
        boolean state = isProvidingPowerPossible(world, x, y, z, metadata);

        if (this.isPowered && !state) {
            world.setBlockAndMetadataWithNotify(x, y, z, MoreGears.COMPARATOR_IDLE.blockID, metadata);
        } else if (!this.isPowered && state) {
            world.setBlockAndMetadataWithNotify(x, y, z, MoreGears.COMPARATOR_ACTIVE.blockID, metadata);
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        //if (!world.isRemote)
        world.scheduleBlockUpdate(x, y, z, MoreGears.COMPARATOR_IDLE.blockID, 0);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        //if (!world.isRemote)
        updateComparator(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
        //if (!world.isRemote)
        updateComparator(world, x, y, z);
    }

    @Override
    public boolean isProvidingPowerPossible(World world, int x, int y, int z, int metadata) {
        float threshold = thresholds[(metadata & 12) >> 2];
        return switch (metadata & 3) {
            case 0 -> howFilled(world, x, y, z + 1) > threshold;
            case 1 -> howFilled(world, x - 1, y, z) > threshold;
            case 2 -> howFilled(world, x, y, z - 1) > threshold;
            case 3 -> howFilled(world, x + 1, y, z) > threshold;
            default -> false;
        };
    }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
        super.blockActivated(world, x, y, z, player);
        //if (!world.isRemote)
        updateComparator(world, x, y, z);
        return true;
    }

    public float howFilled(World world, int x, int y, int z) {
        int bid = world.getBlockId(x, y, z);
        if (Blocks.BLOCKS_LIST[bid] instanceof BlockContainer block) {
            TileEntity te = world.getBlockTileEntity(x, y, z);
            ItemStack item;
            int max = 64;
            int has = 0;
            if (te instanceof IInventory inv) {
                if (te instanceof TileEntityChest) {
                    inv = MoreGears.getChestInventory(world, x, y, z);
                    if (inv != null) {
                        max = inv.getSizeInventory() * 64;
                        for (int i = 0; i < inv.getSizeInventory(); ++i) {
                            item = inv.getStackInSlot(i);
                            if (item != null) has += (64 / item.getItem().getItemStackLimit()) * item.stackSize;
                        }
                    }
                } else if (te instanceof TileEntityFurnace || te instanceof TileEntityRefridgifreezer || te instanceof TileEntityBlastFurnace) {
                    if ((item = inv.getStackInSlot(2)) != null) has = item.stackSize;
                } else if (te instanceof TileEntityIncinerator) {
                    max = 64 * 9;
                    for (int i = 9; i < 18; ++i) {
                        item = inv.getStackInSlot(i);
                        if (item != null) has += (64 / item.getItem().getItemStackLimit()) * item.stackSize;
                    }
                } else {
                    max = inv.getSizeInventory() * inv.getInventoryStackLimit();
                    for (int i = 0; i < inv.getSizeInventory(); ++i) {
                        item = inv.getStackInSlot(i);
                        if (item != null) has += (64 / item.getItem().getItemStackLimit()) * item.stackSize;
                    }
                }
            } else if (te instanceof TileEntityDrawer ted) {
                int itemid = ted.getItemID();

                if (itemid >= 0) {
                    if (ted.isDrawerFull()) return 1.0f;
                    max = DrawerStack.maxCount(itemid);
                    has = ted.getStackSize();
                }
            }

            if (has == 0 || max == 0) return 0.0f;
            return has == max ? 1.0f : (float)has / max;
        }

        return 0.0f;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        this.dropBlockAsItem_do(world, x, y, z, new ItemStack(this.getItemID(), 1, this.damageDropped(metadata)));
        player.addToPlayerScore(null, 1, true);
    }

    @Override
    public int idPicked(World world, int x, int y, int z) { return MoreGears.COMPARATOR_IDLE.getItemID(); }

    @Override
    public int idDropped(int metadata, Random random) { return MoreGears.COMPARATOR_IDLE.getItemID(); }

    @Override
    public boolean notDisplacedByFluids() { return true; }
}
