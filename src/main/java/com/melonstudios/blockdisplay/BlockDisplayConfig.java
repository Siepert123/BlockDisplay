package com.melonstudios.blockdisplay;

import net.minecraftforge.common.config.Config;

@Config(modid = "block_display")
public class BlockDisplayConfig {
    @Config.Comment("Whether to enforce the advanced info from showing without the need of F3+H.")
    public static boolean enforceAdvancedInfo = false;

    @Config.Comment("Whether to disable the info that shows if you can mine the block")
    public static boolean disableMineableInfo = false;
}
