package com.catroidvania.moregears;

import com.fox2code.foxloader.config.ConfigEntry;
import com.fox2code.foxloader.loader.Mod;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.recipe.CraftingManager;

import java.util.Random;

public class MoreGears extends Mod {
    public static final MoreGearsConfig CONFIG = new MoreGearsConfig();
    public static BlockGearFunnel FUNNEL_IDLE;
    public static BlockGearFunnel FUNNEL_ACTIVE;
    public static Random random = new Random();


    @Override
    public void onPreInit() {
        this.setConfigObject(CONFIG);
        FUNNEL_IDLE = new BlockGearFunnel("block_funnel_idle", false);
        FUNNEL_ACTIVE = new BlockGearFunnel("block_funnel_active", true);
        CraftingManager.getInstance().addRecipe(new ItemStack(FUNNEL_IDLE),
                "SGS",
                "S S",
                "SGS",
                'S', Blocks.STONE, 'G', Blocks.GEAR);
    }

    public static class MoreGearsConfig {
        @ConfigEntry(configName = "Enable Funnels", configPath = "enable_funnels")
        public boolean enableFunnels = true;

        //@ConfigEntry(configName = "Funnel Item Limit", configPath = "funnel_item_limit", lowerBounds = 0, upperBounds = 64)
        //public int funnelThroughputMax = 64;
    }
}
