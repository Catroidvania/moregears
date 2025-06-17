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
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.item.data.EnumTools;
import net.minecraft.common.util.Facing;
import net.minecraft.common.util.math.AxisAlignedBB;
import net.minecraft.common.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;


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

        int metadata = world.getBlockMetadata(x, y, z);
        int rot = getOrientation(metadata);
        int tx = x - Facing.offsetXForSide[rot];
        int ty = y - Facing.offsetYForSide[rot];
        int tz = z - Facing.offsetZForSide[rot];

        insertEntityItem(world, tx, ty, tz, rot, entity);
    }

    public EntityItem insertEntityItem(World world, int x, int y, int z, int rot, Entity e) {
        if (world.isRemote || !MoreGears.CONFIG.enableFunnels) return null;

        if (e instanceof EntityItem entity) {
            int bid = world.getBlockId(x, y, z);
            Block target = Blocks.BLOCKS_LIST[bid];
            if (target instanceof BlockContainer) {
                TileEntity container = world.getBlockTileEntity(x, y, z);
                if (container instanceof IInventory inventory) {
                    if (target.blockID == Blocks.CHEST.blockID) {
                        IInventory chestinv = getChestInventory(world, x, y, z);
                        if (chestinv != null) addEntityItemToInventory(chestinv, entity);
                    } else if (
                            container instanceof TileEntityFurnace ||
                            container instanceof TileEntityBlastFurnace ||
                            container instanceof TileEntityRefridgifreezer) {
                        addEntityItemToFurnace(inventory, entity, rot);
                    } else if (container instanceof TileEntityIncinerator) {
                        addEntityItemToIncinerator(inventory, entity, rot);
                    } else {
                        addEntityItemToInventory(inventory, entity);
                    }
                } else if (container instanceof TileEntityDrawer drawer) {
                    addEntityItemToDrawer(drawer, entity);
                }
            }
            if (entity.isDead) {
                return null;
            }
            return entity;
        }
        return null;
    }

    public boolean tryAddEntityItemToSlot(IInventory inventory, EntityItem entity, int slot) {
        ItemStack item = entity.item;
        ItemStack is = inventory.getStackInSlot(slot);

        if (is == null) {
            inventory.setInventorySlotContents(slot, item);
            entity.setEntityDead();
            return false;
        } else if (is.getItem() == item.getItem() && is.isStackable() && is.stackSize != is.getMaxStackSize()) {
            int roomfor = is.getMaxStackSize() - is.stackSize;
            if (item.stackSize <= roomfor) {
                is.stackSize += item.stackSize;
                entity.setEntityDead();
                return false;
            } else {
                is.stackSize = is.getMaxStackSize();
                item.stackSize -= roomfor;
                return true;
            }
        }
        return true;
    }

    public void addEntityItemToFurnace(IInventory inventory, EntityItem entity, int rotation) {
        if (Facing.offsetYForSide[rotation] == 0) {
            tryAddEntityItemToSlot(inventory, entity, 1);
        } else {
            tryAddEntityItemToSlot(inventory, entity, 0);
        }
    }

    public void addEntityItemToIncinerator(IInventory inventory, EntityItem entity, int rotation) {
        if (Facing.offsetYForSide[rotation] == 0) {
            tryAddEntityItemToSlot(inventory, entity, 18);
        } else {
            for (int i = 0; i < 9; ++i) {
                if (!tryAddEntityItemToSlot(inventory, entity, i)) break;
            }
        }
    }

    public void addEntityItemToInventory(IInventory inventory, EntityItem entity) {
        int invsize = inventory.getSizeInventory();

        for (int i = 0; i < invsize; ++i) {
            if (!tryAddEntityItemToSlot(inventory, entity, i)) break;
        }
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

    public void addEntityItemToDrawer(TileEntityDrawer drawer, EntityItem entity) {
        ItemStack item = entity.item;

        int id = item.getItemID();
        if (item.hasTagCompound() || id == Items.POTION.itemID || id == Items.SPLASH_POTION.itemID || id == Items.BOTTLED_FLAME.itemID || id == Items.FLASH_FLASK.itemID) {
            return;
        }
        DrawerStack dstack = drawer.getData();

        if (drawer.getItemID() < 0) {
            DrawerStack.set(dstack, new ItemStack(id, item.stackSize));
            if (!item.isStackable()) {
                dstack.unstackables.add(item);
            }
            entity.setEntityDead();
            return;
        }

        if (id != drawer.getItemID()) return;
        int MAX = DrawerStack.maxCount(id);
        if (drawer.getStackSize() >= MAX) return;

        if (!item.isStackable()) {
            dstack.add(item.stackSize);
            dstack.unstackables.add(item);
            entity.setEntityDead();
        } else  {
            int icount = item.stackSize;
            int roomfor = MAX - icount;

            if (icount <= roomfor) {
                dstack.add(icount);
                entity.setEntityDead();
            } else {
                dstack.add(roomfor);
                item.stackSize -= roomfor;
            }
        }
    }

    public void extractItem(World world, int x, int y, int z) {
        if (world.isRemote || !MoreGears.CONFIG.enableFunnels) return;

        int metadata = world.getBlockMetadata(x, y, z);
        int rot = getOrientation(metadata);

        int tx = x + Facing.offsetXForSide[rot];
        int ty = y + Facing.offsetYForSide[rot];
        int tz = z + Facing.offsetZForSide[rot];

        int bid = world.getBlockId(tx, ty, tz);
        Block target = Blocks.BLOCKS_LIST[bid];

        if (target instanceof BlockContainer) {
            ItemStack item = null;

            TileEntity container = world.getBlockTileEntity(tx, ty, tz);
            if (container instanceof IInventory inventory) {
                if (target.blockID == Blocks.CHEST.blockID) {
                    IInventory chestinv = getChestInventory(world, tx, ty, tz);
                    if (chestinv != null) item = getFirstItemStackFromInventory(chestinv);
                } else if (
                        container instanceof TileEntityFurnace ||
                                container instanceof TileEntityBlastFurnace ||
                                container instanceof TileEntityRefridgifreezer) {
                    item = getFirstItemStackFromInventory(inventory, 2);
                } else if (container instanceof TileEntityIncinerator) {
                    item = getFirstItemStackFromInventory(inventory, 9,18);
                } else {
                    item = getFirstItemStackFromInventory(inventory);
                }
            } else if (container instanceof TileEntityDrawer drawer) {
                item = getItemStackFromDrawer(drawer);
            }

            int ox = x - Facing.offsetXForSide[rot];
            int oy = y - Facing.offsetYForSide[rot];
            int oz = z - Facing.offsetZForSide[rot];

            Block into = Blocks.BLOCKS_LIST[world.getBlockId(ox, oy, oz)];

            if (item != null) {
                if (into instanceof BlockGearConveyorBelt) {
                    dropItemElevated(world, ox, oy, oz, item);
                    world.playAuxSFX(2000, x, y, z, 0);
                } else if (into instanceof BlockContainer) {
                    EntityItem leftovers = insertEntityItem(world, ox, oy, oz, rot, new EntityItem(world, ox, oy, oz, item));
                    if (leftovers != null) {
                        leftovers = insertEntityItem(world, tx, ty, tz, rot, leftovers);
                        if (leftovers != null) {
                            dropBlockAsItem_do(world, ox, oy, oz, item);
                            world.playAuxSFX(2000, x, y, z, 0);
                        }
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
        EntityItem item =new EntityItem(world, tx, ty, tz, itemstack);
        item.delayBeforeCanPickup = 10;
        world.entityJoinedWorld(item);
    }

    public ItemStack getFirstItemStackFromInventory(IInventory inventory) {
        return getFirstItemStackFromInventory(inventory, 0, inventory.getSizeInventory());
    }

    public ItemStack getFirstItemStackFromInventory(IInventory inventory, int slot) {
        return getFirstItemStackFromInventory(inventory, slot, slot+1);
    }

    public ItemStack getFirstItemStackFromInventory(IInventory inventory, int minslot, int maxslot) {
        ItemStack item;
        for (int i = minslot; i < maxslot; ++i) {
            if ((item = inventory.getStackInSlot(i)) != null) {
                inventory.setInventorySlotContents(i, null);
                return item;
            }
        }
        return null;
    }

    public ItemStack getItemStackFromDrawer(TileEntityDrawer drawer) {
        if (drawer.getItemID() == -1) { return null; }
        if (drawer.getStackSize() < 0) { return null; }

        ItemStack item = drawer.getStack();
        ItemStack fetched;
        DrawerStack dstack = drawer.getData();

        if (Items.ITEMS_LIST[item.getItemID()].getItemStackLimit() == 1) {
            fetched = dstack.unstackables.getFirst();
            dstack.subtract(1);
            dstack.unstackables.removeFirst();
            if (drawer.getStackSize() <= 0) DrawerStack.set(dstack, new ItemStack(-1, 0, 0));
        } else {
            //int maxstacksize = Items.ITEMS_LIST[drawer.getStack().getItemID()].getItemStackLimit();
            int count = Math.min(item.getMaxStackSize(), drawer.getStackSize());
            fetched = new ItemStack(item.getItemID(), count);
            dstack.subtract(count);
            if (drawer.getStackSize() <= 0) { DrawerStack.set(dstack, new ItemStack(-1, 0, 0)); }
        }
        return fetched;
    }

    /*
    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        //player.addStat(StatList.BLOCKS_MINED[this.blockID], 1); // crashes
        this.dropBlockAsItem_do(world, x, y, z, new ItemStack(this.getItemID(), 1, this.damageDropped(metadata)));
        player.addToPlayerScore(null, 1, true);
    }

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
        if (world.isRemote) return;

        int metadata = world.getBlockMetadata(x, y, z);
        boolean beingPowered = world.isBlockIndirectlyGettingPowered(x, y, z);

        if (isPowered && !beingPowered) {
            world.setBlockAndMetadataWithNotify(x, y, z, MoreGears.FUNNEL_IDLE.blockID, metadata);
        } else if (!isPowered && beingPowered) {
            world.setBlockAndMetadataWithNotify(x, y, z, MoreGears.FUNNEL_ACTIVE.blockID, metadata);
        }
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
        boolean beingPowered = world.isBlockIndirectlyGettingPowered(x, y, z);
        if (isPowered && !beingPowered) {
            world.setBlockAndMetadataWithNotify(x, y, z, MoreGears.FUNNEL_IDLE.blockID, metadata);
        } else if (!isPowered && beingPowered) {
            world.setBlockAndMetadataWithNotify(x, y, z, MoreGears.FUNNEL_ACTIVE.blockID, metadata);
            extractItem(world, x, y, z);
        }
    }

    @Override
    public int idPicked(World world, int x, int y, int z) { return MoreGears.FUNNEL_IDLE.getItemID(); }

    @Override
    public int idDropped(int metadata, Random random) { return MoreGears.FUNNEL_IDLE.getItemID(); }

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
