package com.catroidvania.moregears;

import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.children.*;
import net.minecraft.common.block.data.Materials;
import net.minecraft.common.block.icon.Icon;
import net.minecraft.common.block.sound.StepSounds;
import net.minecraft.common.block.texture.Face;
import net.minecraft.common.block.tileentity.*;
import net.minecraft.common.entity.Entity;
import net.minecraft.common.entity.EntityLiving;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.entity.inventory.InventoryLargeChest;
import net.minecraft.common.entity.other.EntityItem;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.item.data.EnumTools;
import net.minecraft.common.util.Facing;
import net.minecraft.common.util.math.AxisAlignedBB;
import net.minecraft.common.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import static com.catroidvania.moregears.MoreGears.itemsAreEqual;


public class BlockGearFunnel extends Block {
    public final boolean isPowered;

    public BlockGearFunnel(String id, boolean state) {
        super(id, Materials.MOVABLE_CIRCUIT);
        this.isPowered = state;
        setProperties();
    }

    @Deprecated
    public BlockGearFunnel(int id, boolean state) {
        super(id, Materials.MOVABLE_CIRCUIT);
        this.isPowered = state;
        setProperties();
    }

    public void setProperties() {
        if (isPowered) {
            this.hideFromCreativeMenu();
        }
        this.markMetadataRangeAsUsed(0, 5);
        this.setSound(StepSounds.SOUND_STONE);
        this.setHardness(0.5F);
        this.setRequiresSelfNotify();
        this.setTooltipColor(Color.GRAY.getRGB());
        this.setEffectiveTool(EnumTools.PICKAXE);
    }

    @Override
    protected void allocateTextures() {
        this.addTexture("funnel_top" + (isPowered ? "_powered" : ""), Face.TOP);
        this.addTexture("funnel_side" + (isPowered ? "_powered" : ""), Face.EAST);
        this.addTexture("funnel_bottom", Face.BOTTOM);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        float a = 0.0625F;
        int metadata = world.getBlockMetadata(x, y, z);
        int rot = getOrientation(metadata);
        int xo = Facing.offsetXForSide[rot];
        int yo = Facing.offsetYForSide[rot];
        int zo = Facing.offsetZForSide[rot];

        if (xo < 0) {
            return AxisAlignedBB.getAABBPool().getAABB((float)x + a, (float)y, (float)z, (float)(x + 1), (float)(y + 1), (float)(z + 1));
        } else if (xo > 0) {
            return AxisAlignedBB.getAABBPool().getAABB((float)x, (float)y, (float)z, (float)(x + 1) - a, (float)(y + 1), (float)(z + 1));
        }
        if (yo < 0) {
            return AxisAlignedBB.getAABBPool().getAABB((float)x, (float)y + a, (float)z, (float)(x + 1), (float)(y + 1), (float)(z + 1));
        } else if (yo > 0) {
            return AxisAlignedBB.getAABBPool().getAABB((float)x, (float)y, (float)z, (float)(x + 1), (float)(y + 1) - a, (float)(z + 1));
        }
        if (zo < 0) {
            return AxisAlignedBB.getAABBPool().getAABB((float)x, (float)y, (float)z + a, (float)(x + 1), (float)(y + 1), (float)(z + 1));
        } else if (zo > 0) {
            return AxisAlignedBB.getAABBPool().getAABB((float)x, (float)y, (float)z, (float)(x + 1), (float)(y + 1), (float)(z + 1) - a);
        }

        return AxisAlignedBB.getAABBPool().getAABB((float)x, (float)y, (float)z, (float)(x + 1), (float)(y + 1), (float)(z + 1));
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (world.isRemote || isPowered || !MoreGears.CONFIG.enableFunnels) return;

        if (entity instanceof EntityItem ei) {
            ItemStack item;
            boolean gotSplit = false;
            if (ei.item.stackSize > MoreGears.CONFIG.funnelThroughputMax) {
                item = ei.item.splitStack(MoreGears.CONFIG.funnelThroughputMax);
                gotSplit = true;
            } else {
                item = ei.item;
            }
            int metadata = world.getBlockMetadata(x, y, z);
            int rot = getOrientation(metadata);
            int tx = x - Facing.offsetXForSide[rot];
            int ty = y - Facing.offsetYForSide[rot];
            int tz = z - Facing.offsetZForSide[rot];

            boolean hasLeftovers = insertItemStack(world, tx, ty, tz, rot, item);
            if (!hasLeftovers && !gotSplit) {
                entity.setEntityDead();
            } else if (hasLeftovers && gotSplit) {
                EntityItem newEI = new EntityItem(world, entity.posX, entity.posY, entity.posZ, item);
                newEI.motionX = entity.motionX;
                newEI.motionY = entity.motionY;
                newEI.motionZ = entity.motionZ;
                world.entityJoinedWorld(newEI);
            }
        }
    }

    /// returns true if the stacksize is > 0
    public boolean insertItemStack(World world, int x, int y, int z, int rot, ItemStack item) {
        return insertItemStackRec(world, x, y, z, rot, item, 0);
    }

    public boolean insertItemStackRec(World world, int x, int y, int z, int rot, ItemStack item, int depth) {
        if (world.isRemote || !MoreGears.CONFIG.enableFunnels || MoreGears.CONFIG.funnelThroughputMax == 0) return true;

        int bid = world.getBlockId(x, y, z);
        Block target = Blocks.BLOCKS_LIST[bid];
        if (target instanceof BlockContainer) {
            TileEntity container = world.getBlockTileEntity(x, y, z);
            if (container instanceof IInventory inventory) {
                if (target.blockID == Blocks.CHEST.blockID) {
                    IInventory chestinv = getChestInventory(world, x, y, z);
                    if (chestinv != null) return addItemStackToInventory(chestinv, item);
                } else if (
                        container instanceof TileEntityFurnace ||
                                container instanceof TileEntityBlastFurnace ||
                                container instanceof TileEntityRefridgifreezer) {
                    return addItemStackToFurnace(inventory, item, rot);
                } else if (container instanceof TileEntityIncinerator) {
                    return addItemStackToIncinerator(inventory, item, rot);
                } else {
                    return addItemStackToInventory(inventory, item);
                }
            } else if (container instanceof TileEntityDrawer drawer) {
                return addItemStackToDrawer(drawer, item);
            }
        } else if (isChainable(target) && depth < MoreGears.CONFIG.funnelRedirectLimit) {
            int thisrot = getOrientation(world.getBlockMetadata(x, y, z));
            if (thisrot == Facing.oppositeSide[rot]) {
                return true;
            }
            int tx = x - Facing.offsetXForSide[thisrot];
            int ty = y - Facing.offsetYForSide[thisrot];
            int tz = z - Facing.offsetZForSide[thisrot];
            return insertItemStackRec(world, tx, ty, tz, thisrot, item, ++depth);
        }
        return true;
    }

    public boolean tryAddItemStackToSlot(IInventory inventory, ItemStack item, int slot) {
        ItemStack is = inventory.getStackInSlot(slot);

        if (is == null) {
            inventory.setInventorySlotContents(slot, item);
            return false;
        } else if (itemsAreEqual(item, is) && is.isStackable() && is.stackSize != is.getMaxStackSize()) {
            int roomfor = is.getMaxStackSize() - is.stackSize;
            if (item.stackSize <= roomfor) {
                is.stackSize += item.stackSize;
                return false;
            } else {
                is.stackSize = is.getMaxStackSize();
                item.stackSize -= roomfor;
                return true;
            }
        }
        return true;
    }

    public boolean addItemStackToFurnace(IInventory inventory, ItemStack item, int rotation) {
        if (Facing.offsetYForSide[rotation] == 0) {
            return tryAddItemStackToSlot(inventory, item, 1);
        } else {
            return tryAddItemStackToSlot(inventory, item, 0);
        }
    }

    public boolean addItemStackToIncinerator(IInventory inventory, ItemStack item, int rotation) {
        if (Facing.offsetYForSide[rotation] == 0) {
            return tryAddItemStackToSlot(inventory, item, 18);
        } else {
            for (int i = 0; i < 9; ++i) {
                if (!tryAddItemStackToSlot(inventory, item, i)) return false;
            }
        }
        return true;
    }

    public boolean addItemStackToInventory(IInventory inventory, ItemStack item) {
        int invsize = inventory.getSizeInventory();
        for (int i = 0; i < invsize; ++i) {
            if (!tryAddItemStackToSlot(inventory, item, i)) return false;
        }
        return true;
    }

    public IInventory getChestInventory(World world, int x, int y, int z) {
        if (world.getBlockId(x, y, z) == Blocks.CHEST.blockID) {
            TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity(x, y, z);
            if (world.getBlockId(x - 1, y, z) == Blocks.CHEST.blockID) {
                return new InventoryLargeChest((TileEntityChest) world.getBlockTileEntity(x - 1, y, z), chest);
            }

            if (world.getBlockId(x + 1, y, z) == Blocks.CHEST.blockID) {
                return new InventoryLargeChest(chest, (TileEntityChest) world.getBlockTileEntity(x + 1, y, z));
            }

            if (world.getBlockId(x, y, z - 1) == Blocks.CHEST.blockID) {
                return new InventoryLargeChest((TileEntityChest) world.getBlockTileEntity(x, y, z - 1), chest);
            }

            if (world.getBlockId(x, y, z + 1) == Blocks.CHEST.blockID) {
                return new InventoryLargeChest(chest, (TileEntityChest) world.getBlockTileEntity(x, y, z + 1));
            }
            return chest;
        }
        return null;
    }

    public boolean addItemStackToDrawer(TileEntityDrawer drawer, ItemStack item) {
        int id = item.getItemID();
        if (item.hasTagCompound() || id == Items.POTION.itemID || id == Items.SPLASH_POTION.itemID || id == Items.BOTTLED_FLAME.itemID || id == Items.FLASH_FLASK.itemID) {
            return true;
        }
        DrawerStack dstack = drawer.getData();

        if (drawer.getItemID() < 0) {
            DrawerStack.set(dstack, new ItemStack(id, item.stackSize));
            if (!item.isStackable()) {
                dstack.unstackables.add(item);
            }
            return false;
        }

        int MAX = DrawerStack.maxCount(id);
        if (drawer.getStackSize() >= MAX) return true;

        if (!item.isStackable() && MoreGears.IDsAreEqual(item, dstack.item)) {
            dstack.add(item.stackSize);
            dstack.unstackables.add(item);
            return false;
        } else if (itemsAreEqual(item, dstack.item))  {
            int icount = item.stackSize;
            int roomfor = MAX - icount;

            if (icount <= roomfor) {
                dstack.add(icount);
                return false;
            } else {
                dstack.add(roomfor);
                item.stackSize -= roomfor;
            }
        }
        return true;
    }

    public static boolean isInsertable(Block b) {
        return b instanceof BlockContainer || isChainable(b);
    }

    public static boolean isChainable(Block b) {
        return b instanceof BlockGearFunnel &&
                        MoreGears.CONFIG.funnelRedirectLimit > 0 &&
                        b.blockID != MoreGears.FUNNEL_ACTIVE.blockID &&
                        (MoreGears.CONFIG.idleSiphonChains || b.blockID != MoreGears.SIPHON_IDLE.blockID);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        //player.addStat(StatList.BLOCKS_MINED[this.blockID], 1); // crashes
        this.dropBlockAsItem_do(world, x, y, z, new ItemStack(this.getItemID(), 1, this.damageDropped(metadata)));
        player.addToPlayerScore(null, 1, true);
    }

    /*
    @Override
    public void onBlockDestroyedByExplosion(World world, int x, int y, int z) {
        int metadata = world.getBlockMetadata(x, y, z);
        this.dropBlockAsItem_do(world, x, y, z, new ItemStack(this.getItemID(), 1, this.damageDropped(metadata)));
    }

    @Override
    public boolean dropFromExplosions() { return false; }
    */

    @Override
    public int onBlockPlacedWithOffset(World world, int x, int y, int z, int blockFace, float xVec, float yVec, float zVec, int metadata) {
        int m = metadata & 7 | blockFace;
        world.setBlockMetadataWithNotify(x, y, z, m);
        updateFunnel(world, x, y, z);
        return m;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving player) {
        updateFunnel(world, x, y, z);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        updateFunnel(world, x, y, z);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        updateFunnel(world, x, y, z);
    }

    @Override
    public boolean doWrenchRotation(World world, int x, int y, int z, int metadata, int facing, EntityLiving player) {
        int rot = getOrientation(metadata);
        if (player.isSneaking()) {
            world.setBlockMetadata(x, y, z, Facing.oppositeSide[rot]);
        } else {
            world.setBlockMetadata(x, y, z, ++rot < 6 ? rot : 0);
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int delay) {
        updateFunnel(world, x, y, z);
    }

    public void updateFunnel(World world, int x, int y, int z) {
        if (world.isRemote || !MoreGears.CONFIG.enableFunnels) return;

        int metadata = world.getBlockMetadata(x, y, z);
        boolean beingPowered = world.isBlockGettingPowered(x, y, z);// || world.isBlockIndirectlyGettingPowered(x, y, z);
        if (isPowered && !beingPowered) {
            world.setBlockAndMetadataWithNotify(x, y, z, MoreGears.FUNNEL_IDLE.blockID, metadata);
        } else if (!isPowered && beingPowered) {
            world.setBlockAndMetadataWithNotify(x, y, z, MoreGears.FUNNEL_ACTIVE.blockID, metadata);
        }
    }

    @Override
    public int idPicked(World world, int x, int y, int z) { return this.getItemID(); }

    @Override
    public int idDropped(int metadata, Random random) { return this.getItemID(); }

    /* piston code */
    public static int getOrientation(int metadata) { return metadata & 7; }

    @Override
    public int getRenderType() {
        return 16;
    }

    /*@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving player) {
        int rot = determineOrientation(world, x, y, z, (EntityPlayer)player);
        world.setBlockMetadataWithNotify(x, y, z, rot);
    }*/

    @Override
    public Icon getIcon(int blockFace, int metadata) {
        int rot = getOrientation(metadata);
        if (rot > 5 || blockFace == Face._06.direction()) {
            return super.getIcon(1, 0);
        } else if (blockFace != rot) {
            return blockFace == Facing.oppositeSide[rot] ? super.getIcon(0, 0) : super.getIcon(2, 0);
        } else {
            return super.getIcon(
                    this.minX <= 0.0
                    && this.minY <= 0.0
                    && this.minZ <= 0.0
                    && this.maxX >= 1.0
                    && this.maxY >= 1.0
                    && this.maxZ >= 1.0
                    ? 1
                    : 3,
            0
            );
        }
    }

    @Override
    public boolean notDisplacedByFluids() {
        return true;
    }

    @Override
    public void populateCreativeInventory(ArrayList<ItemStack> itemStacks) {
        itemStacks.add(new ItemStack(this, 1, 0));
    }

    /*
    private static int determineOrientation(World world, int x, int y, int z, EntityPlayer player) {
        if (MathHelper.abs((float)player.posX - x) < 2.0F && MathHelper.abs((float)player.posZ - z) < 2.0F) {
            double playery = player.posY + 1.82 - player.yOffset;
            if (playery - y > 2.0) {
                return 1;
            }

            if (y - playery > 0.0) {
                return 0;
            }
        }

        int rotation = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5) & 3;
        if (rotation == 0) {
            return 2;
        } else if (rotation == 1) {
            return 5;
        } else {
            return rotation == 2 ? 3 : 4;
        }
    }*/

}
