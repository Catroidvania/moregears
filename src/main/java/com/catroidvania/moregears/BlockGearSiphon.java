package com.catroidvania.moregears;

import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.children.BlockContainer;
import net.minecraft.common.block.children.BlockGearConveyorBelt;
import net.minecraft.common.block.texture.Face;
import net.minecraft.common.block.tileentity.*;
import net.minecraft.common.entity.Entity;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.entity.other.EntityItem;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.util.Facing;
import net.minecraft.common.util.math.AxisAlignedBB;
import net.minecraft.common.world.World;

import java.util.Random;


public class BlockGearSiphon extends BlockGearFunnel {

    public BlockGearSiphon(String id, boolean state) {
        super(id, state);
    }

    @Deprecated
    public BlockGearSiphon(int id, boolean state) {
        super(id, state);
    }

    @Override
    protected void allocateTextures() {
        this.addTexture("siphon_top" + (isPowered ? "_powered" : ""), Face.TOP);
        this.addTexture("siphon_side" + (isPowered ? "_powered" : ""), Face.EAST);
        this.addTexture("siphon_bottom", Face.BOTTOM);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
    }

        @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getAABBPool().getAABB((double)x + this.minX, (double)y + this.minY, (double)z + this.minZ, (double)x + this.maxX, (double)y + this.maxY, (double)z + this.maxZ);
    }


    public void extractItem(World world, int x, int y, int z) {
        if (world.isRemote || !MoreGears.CONFIG.enableFunnels || MoreGears.CONFIG.funnelThroughputExtractMax == 0) return;

        int metadata = world.getBlockMetadata(x, y, z);
        int rot = getOrientation(metadata);

        int tx = x + Facing.offsetXForSide[rot];
        int ty = y + Facing.offsetYForSide[rot];
        int tz = z + Facing.offsetZForSide[rot];

        int bid = world.getBlockId(tx, ty, tz);
        Block target = Blocks.BLOCKS_LIST[bid];

        if (target instanceof BlockContainer) {
            ItemStack item = null;
            int cap = MoreGears.CONFIG.funnelThroughputExtractMax;

            TileEntity container = world.getBlockTileEntity(tx, ty, tz);
            if (container instanceof IInventory inventory) {
                if (target.blockID == Blocks.CHEST.blockID) {
                    IInventory chestinv = MoreGears.getChestInventory(world, tx, ty, tz);
                    if (chestinv != null) item = getFirstItemStackFromInventory(chestinv, cap);
                } else if (
                        container instanceof TileEntityFurnace ||
                                container instanceof TileEntityBlastFurnace ||
                                container instanceof TileEntityRefridgifreezer) {
                    item = getFirstItemStackFromInventory(inventory, 2, cap);
                } else if (container instanceof TileEntityIncinerator) {
                    item = getFirstItemStackFromInventory(inventory, 9,18, cap);
                } else {
                    item = getFirstItemStackFromInventory(inventory, cap);
                    if (item != null) world.notifyBlocksOfNeighborChange(x, y, z, Blocks.DRAWER.blockID);
                }
                inventory.onInventoryChanged();
            } else if (container instanceof TileEntityDrawer drawer) {
                item = getItemStackFromDrawer(drawer, cap);
                world.notifyBlocksOfNeighborChange(x, y, z, Blocks.DRAWER.blockID);
            }

            int ox = x - Facing.offsetXForSide[rot];
            int oy = y - Facing.offsetYForSide[rot];
            int oz = z - Facing.offsetZForSide[rot];

            Block into = Blocks.BLOCKS_LIST[world.getBlockId(ox, oy, oz)];

            if (item != null) {
                if (into instanceof BlockGearConveyorBelt) {
                    dropItemElevated(world, ox, oy, oz, item);
                    world.playAuxSFX(2000, x, y, z, 0);
                } else if (isInsertable(into)) {
                    if (insertItemStack(world, ox, oy, oz, rot, item)) {
                        Block back = Blocks.BLOCKS_LIST[world.getBlockId(tx, ty, tz)];
                        if (!isInsertable(back) || insertItemStack(world, tx, ty, tz, rot, item)) {
                            dropBlockAsItem_do(world, ox, oy, oz, item);
                            world.playAuxSFX(2000, x, y, z, 0);
                        }
                    }
                } else if (into instanceof BlockGearFunnel && !isChainable(into)) {
                    Block back = Blocks.BLOCKS_LIST[world.getBlockId(tx, ty, tz)];
                    if (!isInsertable(back) || insertItemStack(world, tx, ty, tz, rot, item)) {
                        dropBlockAsItem_do(world, ox, oy, oz, item);
                        world.playAuxSFX(2000, x, y, z, 0);
                    }
                } else {
                    dropBlockAsItem_do(world, ox, oy, oz, item);
                    world.playAuxSFX(2000, x, y, z, 0);
                }
            } else {
                if (into.blockID == 0) {
                    world.playAuxSFX(1001, x, y, z, 0);
                }
                for(int i = 0; i < 3; ++i) {
                    double particle_x = (double)ox + MoreGears.random.nextDouble();
                    double particle_y = (double)oy + MoreGears.random.nextDouble();
                    double particle_z = (double)oz + MoreGears.random.nextDouble();
                    world.spawnParticle("smoke", particle_x, particle_y, particle_z, -0.1 * Facing.offsetXForSide[rot], -0.1 * Facing.offsetYForSide[rot], -0.1 * Facing.offsetZForSide[rot]);
                }
            }
        }
    }

    public void dropItemElevated(World world, int x, int y, int z, ItemStack itemstack) {
        if (world.isRemote) return;

        double tx = x + 0.5;
        double ty = y + 0.75;
        double tz = z + 0.5;

        EntityItem item = new EntityItem(world, tx, ty, tz, itemstack);
        item.delayBeforeCanPickup = 10;
        item.motionX = 0;
        item.motionY = 0;
        item.motionZ = 0;
        world.entityJoinedWorld(item);
    }

    public ItemStack getFirstItemStackFromInventory(IInventory inventory, int cap) {
        return getFirstItemStackFromInventory(inventory, 0, inventory.getSizeInventory(), cap);
    }

    public ItemStack getFirstItemStackFromInventory(IInventory inventory, int slot, int cap) {
        return getFirstItemStackFromInventory(inventory, slot, slot+1, cap);
    }

    public ItemStack getFirstItemStackFromInventory(IInventory inventory, int minslot, int maxslot, int cap) {
        ItemStack item;
        for (int i = minslot; i < maxslot; ++i) {
            if ((item = inventory.getStackInSlot(i)) != null) {
                if (item.stackSize > cap) {
                    item = item.splitStack(cap);
                } else {
                    inventory.setInventorySlotContents(i, null);
                }
                return item;
            }
        }
        return null;
    }

    public ItemStack getItemStackFromDrawer(TileEntityDrawer drawer, int cap) {
        if (drawer.getItemID() == -1) { return null; }
        if (drawer.getStackSize() < 0 || drawer.getStackSize() <= MoreGears.CONFIG.minDrawerStackSize) { return null; }

        ItemStack item = drawer.getStack();
        ItemStack fetched;
        DrawerStack dstack = drawer.getData();

        if (Items.ITEMS_LIST[item.getItemID()].getItemStackLimit() == 1) {
            fetched = dstack.unstackables.getFirst();
            dstack.subtract(1);
            dstack.unstackables.removeFirst();
            if (drawer.getStackSize() <= 0) DrawerStack.set(dstack, new ItemStack(-1, 0, 0));
        } else {
            int count = Math.min(Math.min(cap, item.getMaxStackSize()), drawer.getStackSize());
            int newsize = drawer.getStackSize() - count;
            if (newsize < MoreGears.CONFIG.minDrawerStackSize) {
                count -= MoreGears.CONFIG.minDrawerStackSize - newsize;
            }
            fetched = new ItemStack(item.getItemID(), count);
            dstack.subtract(count);
            if (drawer.getStackSize() <= 0) { DrawerStack.set(dstack, new ItemStack(-1, 0, 0)); }
        }
        return fetched;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) world.scheduleBlockUpdate(x, y, z, MoreGears.SIPHON_IDLE.blockID, 0);
    }

    @Override
    public void updateFunnel(World world, int x, int y, int z) {
        if (!MoreGears.CONFIG.enableFunnels) return;

        int metadata = world.getBlockMetadata(x, y, z);
        boolean beingPowered = world.isBlockIndirectlyGettingPowered(x, y, z);
        if (isPowered && !beingPowered) {
            world.setBlockAndMetadataWithNotify(x, y, z, MoreGears.SIPHON_IDLE.blockID, metadata);
        } else if (!isPowered && beingPowered) {
            world.setBlockAndMetadataWithNotify(x, y, z, MoreGears.SIPHON_ACTIVE.blockID, metadata);
            extractItem(world, x, y, z);
        }
    }

    @Override
    public int idPicked(World world, int x, int y, int z) { return MoreGears.SIPHON_IDLE.getItemID(); }

    @Override
    public int idDropped(int metadata, Random random) { return MoreGears.SIPHON_IDLE.getItemID(); }
}
