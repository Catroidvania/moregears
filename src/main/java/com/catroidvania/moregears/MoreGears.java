package com.catroidvania.moregears;

import com.fox2code.foxevents.FoxEvents;
import com.fox2code.foxloader.config.ConfigEntry;
import com.fox2code.foxloader.launcher.FoxLauncher;
import com.fox2code.foxloader.loader.Mod;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.tileentity.TileEntityChest;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.entity.inventory.InventoryLargeChest;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.recipe.CraftingManager;
import net.minecraft.common.world.World;
import org.lwjgl.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MoreGears extends Mod {
    public static final MoreGearsConfig CONFIG = new MoreGearsConfig();
    private static final Logger log = LoggerFactory.getLogger(MoreGears.class);
    public static BlockGearFunnel FUNNEL_IDLE;
    public static BlockGearFunnel FUNNEL_ACTIVE;
    public static BlockGearSiphon SIPHON_IDLE;
    public static BlockGearSiphon SIPHON_ACTIVE;
    public static BlockComparator COMPARATOR_IDLE;
    public static BlockComparator COMPARATOR_ACTIVE;
    public static Random random = new Random();


    @Override
    public void onPreInit() {
        this.setConfigObject(CONFIG);

        FUNNEL_IDLE = new BlockGearFunnel("block_funnel_idle", false);
        FUNNEL_ACTIVE = new BlockGearFunnel("block_funnel_active", true);
        CraftingManager.getInstance().addRecipe(new ItemStack(FUNNEL_IDLE),
                "SGS",
                "S S",
                "S S",
                'S', Blocks.STONE, 'G', Blocks.GEAR);

        SIPHON_IDLE = new BlockGearSiphon("block_siphon_idle", false);
        SIPHON_ACTIVE = new BlockGearSiphon("block_siphon_active", true);
        CraftingManager.getInstance().addRecipe(new ItemStack(SIPHON_IDLE),
                "SGS",
                "S S",
                "SGS",
                'S', Blocks.STONE, 'G', Blocks.GEAR);
        COMPARATOR_IDLE = new BlockComparator("block_comparator_idle", false);
        COMPARATOR_ACTIVE = new BlockComparator("block_comparator_active", true);
        CraftingManager.getInstance().addRecipe(new ItemStack(COMPARATOR_IDLE),
                "NGN",
                "NGN",
                "NNN",
                'N', Blocks.NETHERRACK, 'G', Blocks.GEAR);
    }

    public static class MoreGearsConfig {
        @ConfigEntry(configName = "Enable Funnels", configPath = "enable_funnels")
        public boolean enableFunnels = true;

        @ConfigEntry(configName = "Funnel Insert Limit", configPath = "funnel_item_limit", lowerBounds = 1, upperBounds = 64)
        public int funnelThroughputMax = 16;

        @ConfigEntry(configName = "Siphon Extract Limit", configPath = "funnel_item_limit_extract", lowerBounds = 1, upperBounds = 64)
        public int funnelThroughputExtractMax = 4;

        @ConfigEntry(configName = "Funnel Chain Limit", configPath = "funnel_chain_limit", lowerBounds = 0, upperBounds = 64)
        public int funnelRedirectLimit = 16;

        @ConfigEntry(configName = "Unpowered Siphons Chain", configPath = "unpowered_siphon_can_chain")
        public boolean idleSiphonChains = false;

        @ConfigEntry(configName = "Min Drawer Count", configPath = "drawer_min", lowerBounds = 0, upperBounds = 64)
        public int minDrawerStackSize = 1;
    }

    // from drawer code
    public static boolean itemsAreEqual(ItemStack a, ItemStack b) {
        if (a.getItemID() == b.getItemID()) {
            return a.itemDamage == b.itemDamage;
        } else {
            return false;
        }
    }

    public static boolean IDsAreEqual(ItemStack a, ItemStack b) {
        return a.getItemID() == b.getItemID();
    }

    public static IInventory getChestInventory(World world, int x, int y, int z) {
        int bid;

        if ((bid = world.getBlockId(x, y, z)) == Blocks.CHEST.blockID || bid == Blocks.COLORED_CHEST.blockID) {
            TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity(x, y, z);
            if ((bid = world.getBlockId(x - 1, y, z)) == Blocks.CHEST.blockID || bid == Blocks.COLORED_CHEST.blockID) {
                return new InventoryLargeChest((TileEntityChest) world.getBlockTileEntity(x - 1, y, z), chest);
            }

            if ((bid = world.getBlockId(x + 1, y, z)) == Blocks.CHEST.blockID || bid == Blocks.COLORED_CHEST.blockID) {
                return new InventoryLargeChest(chest, (TileEntityChest) world.getBlockTileEntity(x + 1, y, z));
            }

            if ((bid = world.getBlockId(x, y, z - 1)) == Blocks.CHEST.blockID || bid == Blocks.COLORED_CHEST.blockID) {
                return new InventoryLargeChest((TileEntityChest) world.getBlockTileEntity(x, y, z - 1), chest);
            }

            if ((bid = world.getBlockId(x, y, z + 1)) == Blocks.CHEST.blockID || bid == Blocks.COLORED_CHEST.blockID) {
                return new InventoryLargeChest(chest, (TileEntityChest) world.getBlockTileEntity(x, y, z + 1));
            }
            return chest;
        }

        return null;
    }
}
